package org.folio.rest.migration.aspect;

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.folio.rest.migration.aspect.annotation.UpdateMappingRules;
import org.folio.rest.migration.service.OkapiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class MappingRulesAspect {

  private final static Logger logger = LoggerFactory.getLogger(MappingRulesAspect.class);

  @Autowired
  private OkapiService okapiService;

  @Autowired
  private ResourceLoader resourceLoader;

  @Autowired
  private ObjectMapper objectMapper;

  @Before("@annotation(org.folio.rest.migration.aspect.annotation.UpdateMappingRules) && args(..,tenant)")
  public void createReferenceLinks(JoinPoint joinPoint, String tenant) throws IOException {
    String token = okapiService.getToken(tenant);
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    UpdateMappingRules updateMappingRules = signature.getMethod().getAnnotation(UpdateMappingRules.class);
    try {
      JsonNode rules = objectMapper.readValue(loadResource(updateMappingRules.path()).getInputStream(), JsonNode.class);    
      rules = okapiService.updateMappingRules(tenant, token, rules);
      logger.info("updated mapping rules {}", rules);
    } catch (IOException e) {
      logger.error("failed updating mapping rules {}", e.getMessage());
    }
  }

  private Resource loadResource(String path) throws IOException {
    return resourceLoader.getResource(path);
  }

}
