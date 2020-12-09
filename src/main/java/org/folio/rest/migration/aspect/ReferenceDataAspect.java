package org.folio.rest.migration.aspect;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.io.FilenameUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.folio.rest.migration.aspect.annotation.CreateReferenceData;
import org.folio.rest.migration.exception.OkapiRequestException;
import org.folio.rest.migration.model.ReferenceData;
import org.folio.rest.migration.model.ReferenceDatum;
import org.folio.rest.migration.service.OkapiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.stereotype.Component;

@Aspect
@Order(0)
@Component
public class ReferenceDataAspect {

  private final static Logger logger = LoggerFactory.getLogger(ReferenceDataAspect.class);

  @Autowired
  private OkapiService okapiService;

  @Autowired
  private ResourceLoader resourceLoader;

  @Autowired
  private ObjectMapper objectMapper;

  @Before("@annotation(org.folio.rest.migration.aspect.annotation.CreateReferenceData) && args(..,tenant)")
  public void createReferenceLinks(JoinPoint joinPoint, String tenant) throws IOException {
    try {
      String token = okapiService.getToken(tenant);
      MethodSignature signature = (MethodSignature) joinPoint.getSignature();
      CreateReferenceData createReferenceData = signature.getMethod().getAnnotation(CreateReferenceData.class);
      List<ReferenceData> referenceData = loadResources(createReferenceData.pattern()).stream().map(rdr -> {
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
    } catch (OkapiRequestException e) {
      logger.error("failed getting token for tenant {}: {}", tenant, e.getMessage());
    }
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
        logger.warn("failed creating reference data {} {}", referenceData.getName(), e.getMessage());
      }
    }
  }

}
