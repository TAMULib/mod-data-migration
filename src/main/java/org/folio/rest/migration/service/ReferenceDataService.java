package org.folio.rest.migration.service;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.io.FilenameUtils;
import org.folio.rest.migration.model.ReferenceData;
import org.folio.rest.migration.model.ReferenceDatum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;
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

  public void loadReferenceData(String pattern, String tenant) throws IOException {
    String token = okapiService.getToken(tenant);
    List<ReferenceData> referenceData = loadResources(pattern).stream().map(rdr -> {
      Optional<ReferenceData> ord = Optional.empty();
      try {
        ord = Optional.of(objectMapper.readValue(rdr.getInputStream(), ReferenceData.class).withName(FilenameUtils.getBaseName(rdr.getFilename())));
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
  
}
