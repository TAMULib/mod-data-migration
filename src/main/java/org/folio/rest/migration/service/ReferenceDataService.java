package org.folio.rest.migration.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.text.StringSubstitutor;
import org.folio.rest.migration.model.AdditionalReferenceData;
import org.folio.rest.migration.model.ReferenceData;
import org.folio.rest.migration.model.ReferenceDatum;
import org.folio.rest.migration.model.request.ExternalOkapi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class ReferenceDataService {

  private final static Logger logger = LoggerFactory.getLogger(ReferenceDataService.class);

  @Autowired
  private OkapiService okapiService;

  @Autowired
  private ResourceLoader resourceLoader;

  @Autowired
  private ObjectMapper objectMapper;

  @Async("asyncTaskExecutor")
  public CompletableFuture<Void> loadReferenceDataAsync(String pattern, String tenant) {
    return CompletableFuture.supplyAsync(() -> {
      try {
        loadReferenceData(pattern, tenant);
      } catch (IOException e) {
        e.printStackTrace();
        return false;
      }
      return true;
    }).thenAccept(c -> {
      logger.info("finished creating reference data");
    });
  }

  public void loadReferenceData(String pattern, String tenant) throws IOException {
    String token = okapiService.getToken(tenant);
    List<ReferenceData> referenceData = readReferenceData(pattern, tenant, token);
    logger.info("creating reference data; tenant: {}, pattern: {}, quantity: {}", tenant, pattern, referenceData.size());
    loadReferenceData(referenceData);
  }

  public List<ReferenceData> harvestReferenceData(String pattern, ExternalOkapi okapi) throws IOException {
    String token = okapiService.getToken(okapi);
    List<ReferenceData> referenceData = readReferenceData(pattern, okapi.getTenant(), token);
    logger.info("harvesting reference data; tenant: {}, pattern: {}, quantity: {}", okapi.getTenant(), pattern, referenceData.size());
    harvestReferenceData(referenceData, okapi);
    return referenceData;
  }

  private List<ReferenceData> readReferenceData(String pattern, String tenant, String token) throws IOException {
    return loadResources(pattern).stream().map(rdr -> {
      Optional<ReferenceData> ord = Optional.empty();
      String filePath = null;
      try {
        filePath = Paths.get(rdr.getURI()).toAbsolutePath().toString();
      } catch (Exception e) {
        logger.debug("failed reading reference data; file: {}, error: {}", rdr.getFilename(), e.getMessage());
      }
      try {
        ord = Optional.of(objectMapper.readValue(rdr.getInputStream(), ReferenceData.class)
          .withName(FilenameUtils.getBaseName(rdr.getFilename()))
          .withFilePath(filePath));
      } catch (IOException e) {
        logger.error("failed reading reference data; file: {}, error: {}", rdr.getFilename(), e.getMessage());
      }
      return ord;
    }).filter(ord -> ord.isPresent())
      .map(ord -> ord.get().withTenant(tenant).withToken(token))
      .collect(Collectors.toList());
  }

  private List<Resource> loadResources(String pattern) throws IOException {
    return Arrays.asList(ResourcePatternUtils.getResourcePatternResolver(resourceLoader).getResources(pattern));
  }

  private void loadReferenceData(List<ReferenceData> referenceData) {
    Iterator<ReferenceData> rdItr = referenceData.iterator();
    while (rdItr.hasNext()) {
      ReferenceData currRd = rdItr.next();
      boolean depsMet = true;
      for (String dep : currRd.getDependencies()) {
        if (referenceData.stream().map(rd -> rd.getName()).anyMatch(cn -> cn.equals(dep))) {
          depsMet = false;
          break;
        }
      }
      if (depsMet) {
        loadReferenceData(currRd);
        rdItr.remove();
      }
    }
    if (referenceData.size() > 0) {
      logger.info("reference data remaining {}", referenceData.size());
      loadReferenceData(referenceData);
    }
  }

  private void loadReferenceData(ReferenceData referenceData) {
    for (JsonNode data : referenceData.getData()) {
      ReferenceDatum datum = ReferenceDatum.of(referenceData, data);
      try {
        String response = okapiService.loadReferenceData(datum);
        logger.info("created reference data {} {}", referenceData.getName(), response);
      } catch (Exception e) {
        logger.warn("failed creating reference data {}: {}", referenceData.getName(), e.getMessage());
      }

      for (AdditionalReferenceData additional : referenceData.getAdditional()) {
        JsonNode sourceData = data.at(additional.getSource());
        if (sourceData.isArray()) {
          for (JsonNode additionalData : ((ArrayNode) sourceData)) {
            createAdditionalData(referenceData, data, additional, additionalData);
          }
        } else {
          createAdditionalData(referenceData, data, additional, sourceData);
        }
      }
    }
  }

  private void createAdditionalData(ReferenceData referenceData, JsonNode data, AdditionalReferenceData additional, JsonNode additionalData) {
    String tenant = referenceData.getTenant();
    String token = referenceData.getToken();
    Map<String, Object> context = objectMapper.convertValue(data, new TypeReference<Map<String, Object>>(){});
    StringSubstitutor sub = new StringSubstitutor(context);
    String path = sub.replace(additional.getPath());

    ReferenceDatum additionalDatum = ReferenceDatum.of(tenant, token, path, additionalData, referenceData.getAction());

    try {
      String response = okapiService.loadReferenceData(additionalDatum);
      logger.info("created additional reference data {} {} {}", referenceData.getName(), additional.getSource(), response);
    } catch (Exception e) {
      logger.warn("failed creating additional reference data {} {}: {}", referenceData.getName(), additional.getSource(), e.getMessage());
    }
  }

  private void harvestReferenceData(List<ReferenceData> referenceData, ExternalOkapi okapi) {
    referenceData.forEach(datum -> {
      try {
        JsonNode response = okapiService.fetchReferenceData(okapi, datum);
        if (response.has("totalRecords")) {
          Iterator<Entry<String, JsonNode>> nodes = response.fields();
          logger.info("harvested reference data {} {}", datum.getPath(), response);
          while (nodes.hasNext()) {
            Map.Entry<String, JsonNode> entry = (Map.Entry<String, JsonNode>) nodes.next();
            if (!entry.getKey().equals("totalRecords") && !entry.getKey().equals("resultInfo")) {
              List<JsonNode> data = objectMapper.convertValue(entry.getValue(), new TypeReference<ArrayList<JsonNode>>() { });
              datum.setData(data.stream().map(node -> {
                if (datum.getReify()) {
                  String id = node.get("id").asText();
                  node = okapiService.fetchReferenceDataById(okapi, datum, id);
                }
                for (Map.Entry<String, Object> def : datum.getDefaults().entrySet()) {
                  String property = def.getKey();
                  Object value = def.getValue();
                  int lastIndexOf = property.lastIndexOf(".");
                  if (lastIndexOf >= 0) {
                    property = property.substring(lastIndexOf + 1);
                  }
                  if (value instanceof String) {
                    getNode(node, def.getKey()).put(property, (String) value);
                  } else if (value instanceof Integer) {
                    getNode(node, def.getKey()).put(property, (Integer) value);
                  } else if (value instanceof Long) {
                    getNode(node, def.getKey()).put(property, (Long) value);
                  } else if (value instanceof Float) {
                    getNode(node, def.getKey()).put(property, (Float) value);
                  } else if (value instanceof Double) {
                    getNode(node, def.getKey()).put(property, (Double) value);
                  } else if (value instanceof Boolean) {
                    getNode(node, def.getKey()).put(property, (Boolean) value);
                  }
                }
                if (!datum.getTransform().isEmpty()) {
                  ObjectNode transformed = objectMapper.createObjectNode();
                  for (Map.Entry<String, String> te : datum.getTransform().entrySet()) {
                    String key = te.getKey();
                    String value = te.getValue();
                    if (value.equals(".")) {
                      transformed.set(key, node);
                    } else {
                      String property = value;
                      int lastIndexOf = property.lastIndexOf(".");
                      if (lastIndexOf >= 0) {
                        property = property.substring(lastIndexOf + 1);
                      }
                      transformed.set(key, getNode(node, value).get(property));
                    }
                  }
                  node = transformed;
                }
                processExclude(datum, node);
                return node;
              }).collect(Collectors.toList()));
              writeReferenceData(datum);
            }
          }
        } else {
          List<JsonNode> data = new ArrayList<>();
          processExclude(datum, response);
          data.add(response);
          datum.setData(data);
          writeReferenceData(datum);
        }
      } catch (Exception e) {
        logger.warn("failed harvesting reference data {}: {}", datum.getPath(), e.getMessage());
      }
    });
  }

  private ObjectNode getNode(JsonNode input, String path) {
    String[] paths = path.split(Pattern.quote("."));
    if (paths.length == 1) {
      return (ObjectNode) input;
    }
    ObjectNode current = (ObjectNode) input.get(paths[0]);
    if (paths.length == 2) {
      return current;
    }
    return getNode(current, path.substring(path.indexOf(".") + 1));
  }

  private void processExclude(ReferenceData datum, JsonNode node) {
    for (String exclude : datum.getExcludedProperties()) {
      String property = exclude;
      int lastIndexOf = property.lastIndexOf(".");
      if (lastIndexOf >= 0) {
        property = property.substring(lastIndexOf + 1);
      }
      getNode(node, exclude).remove(property);
    }
  }

  private void writeReferenceData(ReferenceData datum) throws JsonGenerationException, JsonMappingException, IOException {
    String filePath = datum.getFilePath().replace("target\\classes", "src\\main\\resources");
    logger.info("writing reference data {}", filePath);
    objectMapper.writerWithDefaultPrettyPrinter()
      .writeValue(new File(filePath), datum);
  }

}
