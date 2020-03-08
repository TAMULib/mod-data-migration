package org.folio.rest.migration.service;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections4.list.UnmodifiableList;
import org.folio.rest.jaxrs.model.Alternativetitletypes;
import org.folio.rest.jaxrs.model.Classificationtypes;
import org.folio.rest.jaxrs.model.Contributornametypes;
import org.folio.rest.jaxrs.model.Contributortypes;
import org.folio.rest.jaxrs.model.Electronicaccessrelationships;
import org.folio.rest.jaxrs.model.Identifiertypes;
import org.folio.rest.jaxrs.model.Instanceformats;
import org.folio.rest.jaxrs.model.Instancenotetypes;
import org.folio.rest.jaxrs.model.Instancetypes;
import org.folio.rest.jaxrs.model.dto.InitJobExecutionsRqDto;
import org.folio.rest.jaxrs.model.dto.InitJobExecutionsRsDto;
import org.folio.rest.jaxrs.model.dto.JobExecution;
import org.folio.rest.jaxrs.model.dto.RawRecordsDto;
import org.folio.rest.migration.config.model.Credentials;
import org.folio.rest.migration.config.model.Okapi;
import org.folio.rest.migration.mapping.MappingParameters;
import org.folio.rest.migration.utility.TimingUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;

@Service
public class OkapiService {

  private static final Logger log = LoggerFactory.getLogger(OkapiService.class);

  private static final int SETTING_LIMIT = 1000;

  @Autowired
  public Okapi okapi;

  // TODO: make into a bean
  public RestTemplate restTemplate;

  public OkapiService() {
    restTemplate = new RestTemplate();
  }

  public String getToken(String tenant) {
    long startTime = System.nanoTime();
    String url = okapi.getUrl() + "/authn/login";
    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("X-Okapi-Tenant", tenant);
    HttpEntity<Credentials> entity = new HttpEntity<>(okapi.getCredentials(), headers);
    ResponseEntity<Credentials> response = restTemplate.exchange(url, HttpMethod.POST, entity, Credentials.class);
    log.debug("get token: {} milliseconds", TimingUtility.getDeltaInMilliseconds(startTime));
    if (response.getStatusCodeValue() == 201) {
      return response.getHeaders().getFirst("X-Okapi-Token");
    }
    throw new RuntimeException("Failed to login: " + response.getStatusCodeValue());
  }

  // TODO: get JsonSchema for mapping-rules
  public JsonNode fetchRules(String tenant, String token) {
    long startTime = System.nanoTime();
    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("X-Okapi-Tenant", tenant);
    headers.set("X-Okapi-Token", token);
    HttpEntity<?> entity = new HttpEntity<>(headers);
    String url = okapi.getUrl() + "/mapping-rules";
    ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.GET, entity, JsonNode.class);
    log.debug("fetch rules: {} milliseconds", TimingUtility.getDeltaInMilliseconds(startTime));
    if (response.getStatusCodeValue() == 200) {
      return response.getBody();
    }
    throw new RuntimeException("Failed to fetch rules: " + response.getStatusCodeValue());
  }

  // TODO: get JsonSchema for hrid-settings
  public JsonNode fetchHridSettings(String tenant, String token) {
    long startTime = System.nanoTime();
    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("X-Okapi-Tenant", tenant);
    headers.set("X-Okapi-Token", token);
    HttpEntity<?> entity = new HttpEntity<>(headers);
    String url = okapi.getUrl() + "/hrid-settings-storage/hrid-settings";
    ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.GET, entity, JsonNode.class);
    log.debug("fetch hrid settings: {} milliseconds", TimingUtility.getDeltaInMilliseconds(startTime));
    if (response.getStatusCodeValue() == 200) {
      return response.getBody();
    }
    throw new RuntimeException("Failed to fetch hrid settings: " + response.getStatusCodeValue());
  }

  public InitJobExecutionsRsDto createJobExecution(String tenant, String token, InitJobExecutionsRqDto jobExecutionDto) {
    long startTime = System.nanoTime();
    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("X-Okapi-Tenant", tenant);
    headers.set("X-Okapi-Token", token);
    HttpEntity<InitJobExecutionsRqDto> entity = new HttpEntity<>(jobExecutionDto, headers);
    String url = okapi.getUrl() + "/change-manager/jobExecutions";
    ResponseEntity<InitJobExecutionsRsDto> response = restTemplate.exchange(url, HttpMethod.POST, entity, InitJobExecutionsRsDto.class);
    log.debug("create job execution: {} milliseconds", TimingUtility.getDeltaInMilliseconds(startTime));
    if (response.getStatusCodeValue() == 201) {
      return response.getBody();
    }
    throw new RuntimeException("Failed to create job execution: " + response.getStatusCodeValue());
  }

  public void finishJobExecution(String tenant, String token, String jobExecutionId, RawRecordsDto rawRecordsDto) {
    long startTime = System.nanoTime();
    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("X-Okapi-Tenant", tenant);
    headers.set("X-Okapi-Token", token);
    HttpEntity<RawRecordsDto> entity = new HttpEntity<>(rawRecordsDto, headers);
    String url = okapi.getUrl() + "/change-manager/jobExecutions/" + jobExecutionId + "/records";
    ResponseEntity<JobExecution> response = restTemplate.exchange(url, HttpMethod.POST, entity, JobExecution.class);
    log.debug("finish job execution: {} milliseconds", TimingUtility.getDeltaInMilliseconds(startTime));
    if (response.getStatusCodeValue() == 204) {
      return;
    }
    throw new RuntimeException("Failed to finish job execution: " + response.getStatusCodeValue());
  }

  public MappingParameters getMappingParamaters(String tenant, String token) {
    long startTime = System.nanoTime();
    final MappingParameters mappingParameters = new MappingParameters();
    // @formatter:off
    Arrays.asList(new ReferenceFetcher[] {
      new ReferenceFetcher("/identifier-types?limit=" + SETTING_LIMIT, Identifiertypes.class,  "identifierTypes"),
      new ReferenceFetcher("/classification-types?limit=" + SETTING_LIMIT, Classificationtypes.class,  "classificationTypes"),
      new ReferenceFetcher("/instance-types?limit=" + SETTING_LIMIT, Instancetypes.class, "instanceTypes"),
      new ReferenceFetcher("/instance-formats?limit=" + SETTING_LIMIT, Instanceformats.class,  "instanceFormats"),
      new ReferenceFetcher("/contributor-types?limit=" + SETTING_LIMIT, Contributortypes.class,  "contributorTypes"),
      new ReferenceFetcher("/contributor-name-types?limit=" + SETTING_LIMIT, Contributornametypes.class,  "contributorNameTypes"),
      new ReferenceFetcher("/electronic-access-relationships?limit=" + SETTING_LIMIT, Electronicaccessrelationships.class,  "electronicAccessRelationships"),
      new ReferenceFetcher("/instance-note-types?limit=" + SETTING_LIMIT, Instancenotetypes.class,  "instanceNoteTypes"),
      new ReferenceFetcher("/alternative-title-types?limit=" + SETTING_LIMIT, Alternativetitletypes.class,  "alternativeTitleTypes")
    }).forEach(fetcher -> {
      HttpHeaders headers = new HttpHeaders();
      headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
      headers.setContentType(MediaType.APPLICATION_JSON);
      headers.set("X-Okapi-Tenant", tenant);
      headers.set("X-Okapi-Token", token);
      HttpEntity<Credentials> entity = new HttpEntity<Credentials>(headers);
      String url = okapi.getUrl() + fetcher.getUrl();
      Class<?> collectionType = fetcher.getCollectionType();
      ResponseEntity<?> response = restTemplate.exchange(url, HttpMethod.GET, entity, collectionType);
      try {
        Field source = collectionType.getDeclaredField(fetcher.getProperty());
        source.setAccessible(true);
        Field target = mappingParameters.getClass().getDeclaredField(fetcher.getProperty());
        target.setAccessible(true);
        target.set(mappingParameters, new UnmodifiableList<>((List<?>) source.get(response.getBody())));
      } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
        e.printStackTrace();
        throw new RuntimeException(e);
      }
    });
    // @formatter:on
    log.debug("get mapping parameters: {} milliseconds", TimingUtility.getDeltaInMilliseconds(startTime));
    return mappingParameters;
  }

  private class ReferenceFetcher {

    private final String url;

    private final Class<?> collectionType;

    private final String property;

    public ReferenceFetcher(String url, Class<?> collectionType, String property) {
      this.url = url;
      this.collectionType = collectionType;
      this.property = property;
    }

    public String getUrl() {
      return url;
    }

    public Class<?> getCollectionType() {
      return collectionType;
    }

    public String getProperty() {
      return property;
    }

  }

}
