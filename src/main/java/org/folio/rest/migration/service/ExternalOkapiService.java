package org.folio.rest.migration.service;

import java.util.Arrays;

import com.fasterxml.jackson.databind.JsonNode;

import org.folio.rest.jaxrs.model.inventory.Servicepoints;
import org.folio.rest.migration.config.model.Credentials;
import org.folio.rest.migration.model.request.ExternalOkapi;
import org.folio.rest.migration.utility.TimingUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ExternalOkapiService {

  private static final Logger log = LoggerFactory.getLogger(ExternalOkapiService.class);

  // TODO: make into a bean
  public RestTemplate restTemplate;

  public ExternalOkapiService() {
    restTemplate = new RestTemplate();
  }

  public String getToken(ExternalOkapi okapi) {
    long startTime = System.nanoTime();
    String url = okapi.getUrl() + "/authn/login";
    HttpEntity<Credentials> entity = new HttpEntity<>(okapi.getCredentials(), headers(okapi.getTenant()));
    ResponseEntity<Credentials> response = restTemplate.exchange(url, HttpMethod.POST, entity, Credentials.class);
    log.debug("get token: {} milliseconds", TimingUtility.getDeltaInMilliseconds(startTime));
    if (response.getStatusCodeValue() == 201) {
      return response.getHeaders().getFirst("X-Okapi-Token");
    }
    throw new RuntimeException("Failed to login: " + response.getStatusCodeValue());
  }

  public Servicepoints fetchServicepoints(ExternalOkapi okapi, String token) {
    long startTime = System.nanoTime();
    HttpEntity<?> entity = new HttpEntity<>(headers(okapi.getTenant(), token));
    String url = okapi.getUrl() + "/service-points?limit=9999";
    ResponseEntity<Servicepoints> response = restTemplate.exchange(url, HttpMethod.GET, entity, Servicepoints.class);
    log.debug("fetch service points: {} milliseconds", TimingUtility.getDeltaInMilliseconds(startTime));
    if (response.getStatusCodeValue() == 200) {
      return response.getBody();
    }
    throw new RuntimeException("Failed to fetch service points: " + response.getStatusCodeValue());
  }

  public JsonNode fetchCalendarPeriodsForServicepoint(ExternalOkapi okapi, String token, String servicePointId) {
    long startTime = System.nanoTime();
    HttpEntity<?> entity = new HttpEntity<>(headers(okapi.getTenant(), token));
    String url = okapi.getUrl() + "/calendar/periods/" + servicePointId + "/period?withOpeningDays=true&showPast=true";
    ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.GET, entity, JsonNode.class);
    log.debug("fetch calendar periods for service point: {} milliseconds", TimingUtility.getDeltaInMilliseconds(startTime));
    if (response.getStatusCodeValue() == 200) {
      return response.getBody();
    }
    throw new RuntimeException("Failed to fetch calendar persiods service point: " + response.getStatusCodeValue());
  }

  private HttpHeaders headers(String tenant, String token) {
    HttpHeaders headers = headers(tenant);
    headers.set("X-Okapi-Token", token);
    return headers;
  }

  // NOTE: assuming all accept and content type will be application/json
  private HttpHeaders headers(String tenant) {
    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN));
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("X-Okapi-Tenant", tenant);
    return headers;
  }

}
