package org.folio.rest.migration.aspect;

import java.io.IOException;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.CodeSignature;
import org.aspectj.lang.reflect.MethodSignature;
import org.folio.rest.migration.aspect.annotation.CreateReferenceData;
import org.folio.rest.migration.service.ReferenceDataService;
import org.folio.spring.tenant.properties.TenantProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Order(0)
@Component
public class ReferenceDataAspect {

  @Autowired
  private TenantProperties tenantProperties;

  @Autowired
  private ReferenceDataService referenceDataService;

  @Before("@annotation(org.folio.rest.migration.aspect.annotation.CreateReferenceData)")
  public void createReferenceData(JoinPoint joinPoint) throws IOException {
    CodeSignature codeSignature = (CodeSignature) joinPoint.getSignature();
    Object[] args = joinPoint.getArgs();
    String[] argNames = codeSignature.getParameterNames();
    Boolean skipReferenceData = false;
    String tenant = tenantProperties.getDefaultTenant();
    for (int i = 0; i < args.length; i++) {
      switch(argNames[i]) {
        case "tenant": tenant = (String) args[i]; break;
        case "skipReferenceData": skipReferenceData = (boolean) args[i]; break;
      }
    }
    if (skipReferenceData) {
      return;
    }
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    CreateReferenceData createReferenceData = signature.getMethod().getAnnotation(CreateReferenceData.class);
    referenceDataService.loadReferenceData(createReferenceData.pattern(), tenant);
  }

}
