package org.folio.rest.migration.service;

import java.io.File;
import java.io.IOException;
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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.apache.commons.io.FilenameUtils;
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
    List<ReferenceData> referenceData = loadResources(pattern).stream().map(rdr -> {
      Optional<ReferenceData> ord = Optional.empty();
      try {
        ord = Optional.of(objectMapper.readValue(rdr.getInputStream(), ReferenceData.class)
          .withName(FilenameUtils.getBaseName(rdr.getFilename()))
          .withFilePath(rdr.getFile().getAbsolutePath()));
      } catch (IOException e) {
        logger.debug("failed reading reference data {}: {}", rdr.getFilename(), e.getMessage());
      }
      return ord;
    }).filter(ord -> ord.isPresent())
      .map(ord -> ord.get().withTenant(tenant).withToken(token))
      .collect(Collectors.toList());
    logger.info("creating reference data");
    createReferenceData(referenceData);
  }

  public List<ReferenceData> harvestReferenceData(String pattern, ExternalOkapi okapi) throws IOException {
    String token = okapiService.getToken(okapi);
    List<ReferenceData> referenceData = loadResources(pattern).stream().map(rdr -> {
      Optional<ReferenceData> ord = Optional.empty();
      try {
        ord = Optional.of(objectMapper.readValue(rdr.getInputStream(), ReferenceData.class)
          .withName(FilenameUtils.getBaseName(rdr.getFilename()))
          .withFilePath(rdr.getFile().getAbsolutePath()));
      } catch (IOException e) {
        logger.debug("failed reading reference data {}: {}", rdr.getFilename(), e.getMessage());
      }
      return ord;
    }).filter(ord -> ord.isPresent())
      .map(ord -> ord.get().withTenant(okapi.getTenant()).withToken(token))
      .collect(Collectors.toList());
    logger.info("harvesting reference data");
    harvestReferenceData(referenceData, okapi);
    return referenceData;
  }

  private List<Resource> loadResources(String pattern) throws IOException {
    return Arrays.asList(ResourcePatternUtils.getResourcePatternResolver(resourceLoader).getResources(pattern));
  }

  private void createReferenceData(List<ReferenceData> referenceData) {
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
        createReferenceData(currRd);
        rdItr.remove();
      }
    }
    if (referenceData.size() > 0) {
      logger.info("reference data remaining {}", referenceData.size());
      createReferenceData(referenceData);
    }
  }

  private void createReferenceData(ReferenceData referenceData) {
    for (JsonNode data : referenceData.getData()) {
      ReferenceDatum datum = ReferenceDatum.of(referenceData, data);
      try {
        JsonNode response = okapiService.createReferenceData(datum);
        logger.info("created reference data {} {}", referenceData.getName(), response);
      } catch (Exception e) {
        logger.warn("failed creating reference data {}: {}", referenceData.getName(), e.getMessage());
      }
    }
  }

  private void harvestReferenceData(List<ReferenceData> referenceData, ExternalOkapi okapi) {
    referenceData.forEach(datum -> {
      try {
        JsonNode response = okapiService.fetchReferenceData(okapi, datum);
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
              for (String exclude : datum.getExcludedProperties()) {
                String property = exclude;
                int lastIndexOf = property.lastIndexOf(".");
                if (lastIndexOf >= 0) {
                  property = property.substring(lastIndexOf + 1);
                }
                getNode(node, exclude).remove(property);
              }
              return node;
            }).collect(Collectors.toList()));
            String filePath = datum.getFilePath().replace("target\\classes", "src\\main\\resources");
            logger.info("writing reference data {}", filePath);
            objectMapper.writerWithDefaultPrettyPrinter()
              .writeValue(new File(filePath), datum);
          }
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

}
