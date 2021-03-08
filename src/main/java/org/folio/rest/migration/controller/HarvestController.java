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
import org.folio.rest.migration.service.OkapiService;
import org.folio.rest.migration.service.ReferenceDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/harvest")
public class HarvestController {

  @Autowired
  private OkapiService okapiService;

  @Autowired
  private ReferenceDataService referenceDataService;

  @Autowired
  private ObjectMapper objectMapper;

  @PostMapping("/calendar")
  public JsonNode harvestServicePointCalendarPeriods(@RequestBody ExternalOkapi okapi) throws IOException {
    ObjectNode response = objectMapper.createObjectNode();
    response.put("okapi", okapi.getUrl());
    response.put("tenant", okapi.getTenant());
    ArrayNode periodsData = response.putArray("openingPeriods");
    String token = okapiService.getToken(okapi);
    Servicepoints servicePoints = okapiService.fetchServicepoints(okapi, token);
    for (Servicepoint servicePoint : servicePoints.getServicepoints()) {
      JsonNode periods = okapiService.fetchCalendarPeriodsForServicepoint(okapi, token, servicePoint.getId());
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

  @PostMapping("/reference-data")
  public JsonNode harvestReferenceData(@RequestBody ExternalOkapi okapi, @RequestParam(required = false, defaultValue = "classpath:/referenceData/**/*.json") String pattern) throws IOException {
    ObjectNode response = objectMapper.createObjectNode();
    response.put("okapi", okapi.getUrl());
    response.put("tenant", okapi.getTenant());
    referenceDataService.harvestReferenceData(pattern, okapi);
    return response;
  }

}
