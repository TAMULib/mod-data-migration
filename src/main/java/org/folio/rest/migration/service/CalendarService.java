package org.folio.rest.migration.service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import org.apache.commons.io.FilenameUtils;
import org.folio.rest.migration.model.ReferenceDatum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class CalendarService {

  private final static Logger logger = LoggerFactory.getLogger(CalendarService.class);

  private final static String OPENING_PERIODS = "openingPeriods";

  private final static String CALENDAR_PERIOD_PATH_TEMPLATE = "/calendar/periods/%s/period";

  @Autowired
  private OkapiService okapiService;

  @Autowired
  private ResourceLoader resourceLoader;

  @Autowired
  private ObjectMapper objectMapper;

  @Async("asyncTaskExecutor")
  public CompletableFuture<Void> createCalendarPeriodsAsync(String pattern, String tenant) {
    return CompletableFuture.supplyAsync(() -> {
      try {
        return createCalendarPeriods(pattern, tenant);
      } catch (IOException e) {
        e.printStackTrace();
        return false;
      }
    }).thenAccept(c -> {
      logger.info("finished create calendar periods");
    });
  }

  public boolean createCalendarPeriods(String pattern, String tenant) throws IOException {
    String token = okapiService.getToken(tenant);
    for (Resource resource : loadResources(pattern)) {
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
    return true;
  }

  private List<Resource> loadResources(String pattern) throws IOException {
    return Arrays.asList(ResourcePatternUtils.getResourcePatternResolver(resourceLoader).getResources(pattern));
  }

}
