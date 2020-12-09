package org.folio.rest.migration.aspect;

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.folio.rest.migration.aspect.annotation.UpdateRules;
import org.folio.rest.migration.exception.OkapiRequestException;
import org.folio.rest.migration.service.OkapiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

@Aspect
@Order(1)
@Component
public class RulesAspect {

  private final static Logger logger = LoggerFactory.getLogger(RulesAspect.class);

  @Autowired
  private OkapiService okapiService;

  @Autowired
  private ResourceLoader resourceLoader;

  @Autowired
  private ObjectMapper objectMapper;

  @Before("@annotation(org.folio.rest.migration.aspect.annotation.UpdateRules) && args(..,tenant)")
  public void updateRules(JoinPoint joinPoint, String tenant) throws IOException {
    String token;
    try {
      token = okapiService.getToken(tenant);
    } catch (OkapiRequestException e) {
      logger.error("failed getting token for tenant {}: {}", tenant, e.getMessage());
      return;
    }
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
      UpdateRules updateRules = signature.getMethod().getAnnotation(UpdateRules.class);
      try {
        JsonNode rules = objectMapper.readValue(loadResource(updateRules.file()).getInputStream(), JsonNode.class);
        okapiService.updateRules(rules, updateRules.path(), tenant, token);
        logger.info("updated mapping rules {}", rules);
      } catch (IOException e) {
        logger.error("failed reading resource {}: {}", updateRules.file(), e.getMessage());
      } catch (OkapiRequestException e) {
        logger.debug("failed updating mapping rules: {}", e.getMessage());
      }
  }

  private Resource loadResource(String path) throws IOException {
    return resourceLoader.getResource(path);
  }

}
