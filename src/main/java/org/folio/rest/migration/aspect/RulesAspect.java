package org.folio.rest.migration.aspect;

import java.io.IOException;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.folio.rest.migration.aspect.annotation.UpdateRules;
import org.folio.rest.migration.service.RulesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Order(1)
@Component
public class RulesAspect {

  @Autowired
  private RulesService rulesService;

  @Before("@annotation(org.folio.rest.migration.aspect.annotation.UpdateRules) && args(..,tenant,skipReferenceData,skipRules)")
  public void updateRules(JoinPoint joinPoint, String tenant, boolean skipReferenceData, boolean skipRules) throws IOException {
    if (skipRules) {
      return;
    }
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    UpdateRules updateRules = signature.getMethod().getAnnotation(UpdateRules.class);
    rulesService.updateRules(updateRules.file(), updateRules.path(), tenant);
  }

}
