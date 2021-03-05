package org.folio.rest.migration.aspect;

import java.io.IOException;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.folio.rest.migration.aspect.annotation.CreateReferenceData;
import org.folio.rest.migration.service.ReferenceDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Order(0)
@Component
public class ReferenceDataAspect {

  @Autowired
  private ReferenceDataService referenceDataService;

  @Before("@annotation(org.folio.rest.migration.aspect.annotation.CreateReferenceData) && args(..,tenant,skipReferenceData)")
  public void createReferenceData(JoinPoint joinPoint, String tenant, boolean skipReferenceData) throws IOException {
    if (skipReferenceData) {
      return;
    }
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    CreateReferenceData createReferenceData = signature.getMethod().getAnnotation(CreateReferenceData.class);
    referenceDataService.loadReferenceData(createReferenceData.pattern(), tenant);
  }

}
