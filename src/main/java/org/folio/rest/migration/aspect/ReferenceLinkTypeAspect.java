package org.folio.rest.migration.aspect;

import java.io.IOException;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.folio.rest.migration.aspect.annotation.CreateReferenceLinkTypes;
import org.folio.rest.model.ReferenceLinkType;
import org.folio.rest.model.repo.ReferenceLinkTypeRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

@Aspect
@Component
public class ReferenceLinkTypeAspect {

  private final static Logger logger = LoggerFactory.getLogger(ReferenceLinkTypeAspect.class);

  @Autowired
  private ReferenceLinkTypeRepo referenceLinkTypeRepo;

  @Autowired
  private ResourceLoader resourceLoader;

  @Autowired
  private ObjectMapper objectMapper;

  @Before("@annotation(org.folio.rest.migration.aspect.annotation.CreateReferenceLinkTypes)")
  public void createReferenceLinks(JoinPoint joinPoint) throws IOException {
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    CreateReferenceLinkTypes createReferenceLinkTypes = signature.getMethod().getAnnotation(CreateReferenceLinkTypes.class);
    for (Resource referenceLinkTypeResource : loadResources(createReferenceLinkTypes.path())) {
      ReferenceLinkType referenceLinkType = objectMapper.readValue(referenceLinkTypeResource.getInputStream(), ReferenceLinkType.class);
      if (referenceLinkTypeRepo.existsById(referenceLinkType.getId())) {
        logger.info("reference link type with id {} already exists", referenceLinkType.getId());
      } else if (referenceLinkTypeRepo.existsByName(referenceLinkType.getName())) {
        logger.info("reference link type with name {} already exists", referenceLinkType.getName());
      } else {
        referenceLinkTypeRepo.save(referenceLinkType);
        logger.info("created reference link type {} ({})", referenceLinkType.getName(), referenceLinkType.getId());
      }
    }
  }

  private Resource[] loadResources(String pattern) throws IOException {
    return ResourcePatternUtils.getResourcePatternResolver(resourceLoader).getResources(pattern);
  }

}
