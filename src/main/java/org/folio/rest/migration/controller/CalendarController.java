package org.folio.rest.migration.controller;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.folio.rest.jaxrs.model.inventory.Servicepoint;
import org.folio.rest.jaxrs.model.inventory.Servicepoints;
import org.folio.rest.migration.model.request.ExternalOkapi;
import org.folio.rest.migration.service.ExternalOkapiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/calendar")
public class CalendarController {

  @Autowired
  private ExternalOkapiService externalOkapiService;

  @Autowired
  private ObjectMapper objectMapper;

  @PostMapping("/harvest")
  public JsonNode harvestServicePointCalendarPeriods(@RequestBody ExternalOkapi okapi) throws IOException {
    ObjectNode response = objectMapper.createObjectNode();
    response.put("okapi", okapi.getUrl());
    response.put("tenant", okapi.getTenant());
    ArrayNode periodsData = response.putArray("openingPeriods");
    String token = externalOkapiService.getToken(okapi);
    Servicepoints servicePoints = externalOkapiService.fetchServicepoints(okapi, token);
    for (Servicepoint servicePoint : servicePoints.getServicepoints()) {
      JsonNode periods = externalOkapiService.fetchCalendarPeriodsForServicepoint(okapi, token, servicePoint.getId());
      File calendarFile = new File(String.format("src/main/resources/calendar/%s.json", servicePoint.getId()));
      objectMapper.writerWithDefaultPrettyPrinter().writeValue(calendarFile, periods);
      ObjectNode periodData = objectMapper.createObjectNode();
      periodData.put("servicePointId", servicePoint.getId());
      periodData.put("totalRecords", periods.get("totalRecords").asInt());
      periodsData.add(periodData);
    }
    response.put("totalRecords", servicePoints.getTotalRecords());
    return response;
  }

}
