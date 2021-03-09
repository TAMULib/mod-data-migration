package org.folio.rest.migration.aspect;

import java.io.IOException;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.CodeSignature;
import org.aspectj.lang.reflect.MethodSignature;
import org.folio.rest.migration.aspect.annotation.CreateCalendarPeriods;
import org.folio.rest.migration.service.CalendarService;
import org.folio.spring.tenant.properties.TenantProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Order(2)
@Component
public class CalendarPeriodAspect {

  @Autowired
  private TenantProperties tenantProperties;

  @Autowired
  private CalendarService calendarService;

  @Before("@annotation(org.folio.rest.migration.aspect.annotation.CreateCalendarPeriods)")
  public void createCalendarPeriods(JoinPoint joinPoint) throws IOException {
    CodeSignature codeSignature = (CodeSignature) joinPoint.getSignature();
    Object[] args = joinPoint.getArgs();
    String[] argNames = codeSignature.getParameterNames();
    Boolean skipCalendarPeriods = false;
    String tenant = tenantProperties.getDefaultTenant();
    for (int i = 0; i < args.length; i++) {
      switch(argNames[i]) {
        case "tenant": tenant = (String) args[i]; break;
        case "skipCalendarPeriods": skipCalendarPeriods = (boolean) args[i]; break;
      }
    }
    if (skipCalendarPeriods) {
      return;
    }
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    CreateCalendarPeriods createCalendarPeriods = signature.getMethod().getAnnotation(CreateCalendarPeriods.class);
    calendarService.createCalendarPeriods(createCalendarPeriods.pattern(), tenant);
  }

}
