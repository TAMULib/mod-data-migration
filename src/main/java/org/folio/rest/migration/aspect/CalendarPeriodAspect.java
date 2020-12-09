package org.folio.rest.migration.aspect;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import org.apache.commons.io.FilenameUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.folio.rest.migration.aspect.annotation.CreateCalendarPeriods;
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
@Order(2)
@Component
public class CalendarPeriodAspect {

  private final static Logger logger = LoggerFactory.getLogger(CalendarPeriodAspect.class);

  private final static String OPENING_PERIODS = "openingPeriods";

  private final static String CALENDAR_PERIOD_PATH_TEMPLATE = "/calendar/periods/%s/period";

  @Autowired
  private OkapiService okapiService;

  @Autowired
  private ResourceLoader resourceLoader;

  @Autowired
  private ObjectMapper objectMapper;

  @Before("@annotation(org.folio.rest.migration.aspect.annotation.CreateCalendarPeriods) && args(..,tenant)")
  public void createCalendarPeriods(JoinPoint joinPoint, String tenant) throws IOException {
    String token = okapiService.getToken(tenant);
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    CreateCalendarPeriods createCalendarPeriods = signature.getMethod().getAnnotation(CreateCalendarPeriods.class);
    for (Resource resource : loadResources(createCalendarPeriods.pattern())) {
      String servicePointId = FilenameUtils.removeExtension(resource.getFilename());
      String path = String.format(CALENDAR_PERIOD_PATH_TEMPLATE, servicePointId);
      JsonNode periods = objectMapper.readTree(resource.getInputStream());
      ((ArrayNode) periods.get(OPENING_PERIODS)).forEach(data -> {
        ReferenceDatum referenceDatum = ReferenceDatum.of(tenant, token, path, data);
        try {
          JsonNode response = okapiService.createReferenceData(referenceDatum);
          logger.info("created calendar period for service point {}: {}", servicePointId, response);
        } catch (Exception e) {
          logger.debug("failed creating calendar period for service point {}: {}", servicePointId, e.getMessage());
        }
      });
    }
  }

  private List<Resource> loadResources(String pattern) throws IOException {
    return Arrays.asList(ResourcePatternUtils.getResourcePatternResolver(resourceLoader).getResources(pattern));
  }

}
