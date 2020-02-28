package org.folio.rest.migration.service;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections4.list.UnmodifiableList;
import org.folio.rest.migration.config.model.Credentials;
import org.folio.rest.migration.config.model.Okapi;
import org.folio.rest.migration.mapping.MappingParameters;
import org.folio.rest.migration.model.generated.settings.AlternativeTitleTypes;
import org.folio.rest.migration.model.generated.settings.ClassificationTypes;
import org.folio.rest.migration.model.generated.settings.ContributorNameTypes;
import org.folio.rest.migration.model.generated.settings.ContributorTypes;
import org.folio.rest.migration.model.generated.settings.ElectronicAccessRelationships;
import org.folio.rest.migration.model.generated.settings.IdentifierTypes;
import org.folio.rest.migration.model.generated.settings.InstanceFormats;
import org.folio.rest.migration.model.generated.settings.InstanceNoteTypes;
import org.folio.rest.migration.model.generated.settings.InstanceTypes;
import org.folio.rest.migration.model.generated.source_record_manager.InitJobExecutionsRqDto;
import org.folio.rest.migration.model.generated.source_record_manager.InitJobExecutionsRsDto;
import org.folio.rest.migration.model.generated.source_record_manager.JobExecution;
import org.folio.rest.migration.model.generated.source_record_manager.RawRecordsDto;
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
    log.info("get token time: " + TimingUtility.getDeltaInMilliseconds(startTime) + " milliseconds");
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
    log.info("fetch rules time: " + TimingUtility.getDeltaInMilliseconds(startTime) + " milliseconds");
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
    log.info("fetch hrid settings time: " + TimingUtility.getDeltaInMilliseconds(startTime) + " milliseconds");
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
    log.info("create job execution time: " + TimingUtility.getDeltaInMilliseconds(startTime) + " milliseconds");
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
    log.info("finish job execution time: " + TimingUtility.getDeltaInMilliseconds(startTime) + " milliseconds");
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
      new ReferenceFetcher("/identifier-types?limit=" + SETTING_LIMIT, IdentifierTypes.class,  "identifierTypes"),
      new ReferenceFetcher("/classification-types?limit=" + SETTING_LIMIT, ClassificationTypes.class,  "classificationTypes"),
      new ReferenceFetcher("/instance-types?limit=" + SETTING_LIMIT, InstanceTypes.class, "instanceTypes"),
      new ReferenceFetcher("/instance-formats?limit=" + SETTING_LIMIT, InstanceFormats.class,  "instanceFormats"),
      new ReferenceFetcher("/contributor-types?limit=" + SETTING_LIMIT, ContributorTypes.class,  "contributorTypes"),
      new ReferenceFetcher("/contributor-name-types?limit=" + SETTING_LIMIT, ContributorNameTypes.class,  "contributorNameTypes"),
      new ReferenceFetcher("/electronic-access-relationships?limit=" + SETTING_LIMIT, ElectronicAccessRelationships.class,  "electronicAccessRelationships"),
      new ReferenceFetcher("/instance-note-types?limit=" + SETTING_LIMIT, InstanceNoteTypes.class,  "instanceNoteTypes"),
      new ReferenceFetcher("/alternative-title-types?limit=" + SETTING_LIMIT, AlternativeTitleTypes.class,  "alternativeTitleTypes")
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
    log.info("get mapping parameters time: " + TimingUtility.getDeltaInMilliseconds(startTime) + " milliseconds");
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
