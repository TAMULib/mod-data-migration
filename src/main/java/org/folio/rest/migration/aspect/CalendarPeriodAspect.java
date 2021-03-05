package org.folio.rest.migration.aspect;

import java.io.IOException;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.folio.rest.migration.aspect.annotation.CreateCalendarPeriods;
import org.folio.rest.migration.service.CalendarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Order(2)
@Component
public class CalendarPeriodAspect {

  @Autowired
  private CalendarService calendarService;

  @Before("@annotation(org.folio.rest.migration.aspect.annotation.CreateCalendarPeriods) && args(..,tenant,skipReferenceData,skipRules,skipCalendarPeriods)")
  public void createCalendarPeriods(JoinPoint joinPoint, String tenant, boolean skipReferenceData, boolean skipRules, boolean skipCalendarPeriods) throws IOException {
    if (skipCalendarPeriods) {
      return;
    }
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    CreateCalendarPeriods createCalendarPeriods = signature.getMethod().getAnnotation(CreateCalendarPeriods.class);
    calendarService.createCalendarPeriods(createCalendarPeriods.pattern(), tenant);
  }

}
