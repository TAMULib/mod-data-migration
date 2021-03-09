package org.folio.rest.migration.aspect;

import java.io.IOException;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.CodeSignature;
import org.aspectj.lang.reflect.MethodSignature;
import org.folio.rest.migration.aspect.annotation.UpdateRules;
import org.folio.rest.migration.service.RulesService;
import org.folio.spring.tenant.properties.TenantProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Order(1)
@Component
public class RulesAspect {

  @Autowired
  private TenantProperties tenantProperties;

  @Autowired
  private RulesService rulesService;

  @Before("@annotation(org.folio.rest.migration.aspect.annotation.UpdateRules)")
  public void updateRules(JoinPoint joinPoint) throws IOException {
    CodeSignature codeSignature = (CodeSignature) joinPoint.getSignature();
    Object[] args = joinPoint.getArgs();
    String[] argNames = codeSignature.getParameterNames();
    Boolean skipRules = false;
    String tenant = tenantProperties.getDefaultTenant();
    for (int i = 0; i < args.length; i++) {
      switch(argNames[i]) {
        case "tenant": tenant = (String) args[i]; break;
        case "skipRules": skipRules = (boolean) args[i]; break;
      }
    }
    if (skipRules) {
      return;
    }
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    UpdateRules updateRules = signature.getMethod().getAnnotation(UpdateRules.class);
    rulesService.updateRules(updateRules.file(), updateRules.path(), tenant);
  }

}
