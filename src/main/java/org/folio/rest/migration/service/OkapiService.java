package org.folio.rest.migration.service;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;

import org.apache.commons.collections4.list.UnmodifiableList;
import org.folio.Alternativetitletypes;
import org.folio.Classificationtypes;
import org.folio.Contributornametypes;
import org.folio.Contributortypes;
import org.folio.Electronicaccessrelationships;
import org.folio.Identifiertypes;
import org.folio.Instanceformats;
import org.folio.Instancenotetypes;
import org.folio.Instancetypes;
import org.folio.Issuancemodes;
import org.folio.processing.mapping.defaultmapper.processor.parameters.MappingParameters;
import org.folio.rest.jaxrs.model.circulation.CheckOutByBarcodeRequest;
import org.folio.rest.jaxrs.model.circulation.Loan;
import org.folio.rest.jaxrs.model.dataimport.common.Status;
import org.folio.rest.jaxrs.model.dataimport.dto.InitJobExecutionsRqDto;
import org.folio.rest.jaxrs.model.dataimport.dto.InitJobExecutionsRsDto;
import org.folio.rest.jaxrs.model.dataimport.dto.JobExecution;
import org.folio.rest.jaxrs.model.dataimport.dto.JobExecution.UiStatus;
import org.folio.rest.jaxrs.model.dataimport.dto.RawRecordsDto;
import org.folio.rest.jaxrs.model.dataimport.mod_data_import_converter_storage.JobProfile;
import org.folio.rest.jaxrs.model.dataimport.mod_data_import_converter_storage.JobProfileCollection;
import org.folio.rest.jaxrs.model.dataimport.mod_data_import_converter_storage.JobProfileUpdateDto;
import org.folio.rest.jaxrs.model.inventory.Holdingsrecord;
import org.folio.rest.jaxrs.model.inventory.Instance;
import org.folio.rest.jaxrs.model.inventory.Instancerelationship;
import org.folio.rest.jaxrs.model.inventory.Item;
import org.folio.rest.jaxrs.model.inventory.Items;
import org.folio.rest.jaxrs.model.inventory.Loantypes;
import org.folio.rest.jaxrs.model.inventory.Locations;
import org.folio.rest.jaxrs.model.inventory.Materialtypes;
import org.folio.rest.jaxrs.model.inventory.Servicepoints;
import org.folio.rest.jaxrs.model.inventory.Statisticalcodes;
import org.folio.rest.jaxrs.model.userimport.schemas.ImportResponse;
import org.folio.rest.jaxrs.model.userimport.schemas.UserdataimportCollection;
import org.folio.rest.jaxrs.model.users.AddresstypeCollection;
import org.folio.rest.jaxrs.model.users.Userdata;
import org.folio.rest.jaxrs.model.users.UserdataCollection;
import org.folio.rest.jaxrs.model.users.Usergroups;
import org.folio.rest.migration.config.model.Credentials;
import org.folio.rest.migration.config.model.Okapi;
import org.folio.rest.migration.model.ReferenceDatum;
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

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.jackson.DatabindCodec;

@Service
public class OkapiService {

  private static final Logger log = LoggerFactory.getLogger(OkapiService.class);

  private static final int SETTING_LIMIT = 1000;

  @Autowired
  public Okapi okapi;

  public RestTemplate restTemplate;

  public OkapiService() {
    restTemplate = new RestTemplate();
  }

  public String getToken(String tenant) {
    long startTime = System.nanoTime();
    String url = okapi.getUrl() + "/authn/login";
    HttpEntity<Credentials> entity = new HttpEntity<>(okapi.getCredentials(), headers(tenant));
    ResponseEntity<Credentials> response = restTemplate.exchange(url, HttpMethod.POST, entity, Credentials.class);
    log.debug("get token: {} milliseconds", TimingUtility.getDeltaInMilliseconds(startTime));
    if (response.getStatusCodeValue() == 201) {
      return response.getHeaders().getFirst("X-Okapi-Token");
    }
    log.error("Failed to login: " + response.getStatusCodeValue());
    throw new RuntimeException("Failed to login: " + response.getStatusCodeValue());
  }

  public JsonNode createReferenceData(ReferenceDatum referenceDatum) {
    long startTime = System.nanoTime();
    String url = okapi.getUrl() + referenceDatum.getPath();
    HttpEntity<JsonNode> entity = new HttpEntity<>(referenceDatum.getData(), headers(referenceDatum.getTenant(), referenceDatum.getToken()));
    ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.POST, entity, JsonNode.class);
    log.debug("create reference data: {} milliseconds", TimingUtility.getDeltaInMilliseconds(startTime));
    if (response.getStatusCodeValue() == 201) {
      return response.getBody();
    }
    log.error("Failed to create reference data: " + response.getStatusCodeValue());
    throw new RuntimeException("Failed to create reference data: " + response.getStatusCodeValue());
  }

  public Loan checkoutByBarcode(CheckOutByBarcodeRequest request, String tenant, String token) {
    long startTime = System.nanoTime();
    String url = okapi.getUrl() + "/circulation/check-out-by-barcode";
    HttpEntity<CheckOutByBarcodeRequest> entity = new HttpEntity<>(request, headers(tenant, token));
    ResponseEntity<Loan> response = restTemplate.exchange(url, HttpMethod.POST, entity, Loan.class);
    log.debug("checkout by barcode: {} milliseconds", TimingUtility.getDeltaInMilliseconds(startTime));
    if (response.getStatusCodeValue() == 201) {
      return response.getBody();
    }
    log.error("Failed to checkout by barcode: " + response.getStatusCodeValue());
    throw new RuntimeException("Failed to checkout by barcode: " + response.getStatusCodeValue());
  }

  public void updateLoan(JsonNode loan, String tenant, String token) {
    long startTime = System.nanoTime();
    String url = okapi.getUrl() + "/circulation/loans/" + loan.get("id").asText();
    HttpEntity<?> entity = new HttpEntity<>(loan, headers(tenant, token));
    ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.PUT, entity, Void.class);
    log.debug("update loan: {} milliseconds", TimingUtility.getDeltaInMilliseconds(startTime));
    if (response.getStatusCodeValue() < 200 || response.getStatusCodeValue() > 204) {
      log.error("Failed to create job execution: " + response.getStatusCodeValue());
      throw new RuntimeException("Failed to update loan: " + response.getStatusCodeValue());
    }
  }

  public Servicepoints fetchServicepoints(String tenant, String token) {
    long startTime = System.nanoTime();
    HttpEntity<?> entity = new HttpEntity<>(headers(tenant, token));
    String url = okapi.getUrl() + "/service-points?limit=9999";
    ResponseEntity<Servicepoints> response = restTemplate.exchange(url, HttpMethod.GET, entity, Servicepoints.class);
    log.debug("fetch service points: {} milliseconds", TimingUtility.getDeltaInMilliseconds(startTime));
    if (response.getStatusCodeValue() == 200) {
      return response.getBody();
    }
    log.error("Failed to fetch service points: " + response.getStatusCodeValue());
    throw new RuntimeException("Failed to fetch service points: " + response.getStatusCodeValue());
  }

  public Userdata lookupUser(String tenant, String token, String username) {
    long startTime = System.nanoTime();
    HttpEntity<?> entity = new HttpEntity<>(headers(tenant, token));
    String url = okapi.getUrl() + "/users?query=username==" + username;
    ResponseEntity<UserdataCollection> response = restTemplate.exchange(url, HttpMethod.GET, entity, UserdataCollection.class);
    log.debug("lookup user: {} milliseconds", TimingUtility.getDeltaInMilliseconds(startTime));
    if (response.getStatusCodeValue() == 200) {
      UserdataCollection userCollection = response.getBody();
      if (userCollection.getTotalRecords() > 0) {
        return userCollection.getUsers().get(0);
      }
      log.error("User with username " + username + " not found");
      throw new RuntimeException("User with username " + username + " not found");
    }
    log.error("Failed to lookup user: " + response.getStatusCodeValue());
    throw new RuntimeException("Failed to lookup user: " + response.getStatusCodeValue());
  }

  public Usergroups fetchUsergroups(String tenant, String token) {
    long startTime = System.nanoTime();
    HttpEntity<?> entity = new HttpEntity<>(headers(tenant, token));
    String url = okapi.getUrl() + "/groups?limit=9999";
    ResponseEntity<Usergroups> response = restTemplate.exchange(url, HttpMethod.GET, entity, Usergroups.class);
    log.debug("fetch user groups: {} milliseconds", TimingUtility.getDeltaInMilliseconds(startTime));
    if (response.getStatusCodeValue() == 200) {
      return response.getBody();
    }
    log.error("Failed to fetch user groups: " + response.getStatusCodeValue());
    throw new RuntimeException("Failed to fetch user groups: " + response.getStatusCodeValue());
  }

  public AddresstypeCollection fetchAddresstypes(String tenant, String token) {
    long startTime = System.nanoTime();
    HttpEntity<?> entity = new HttpEntity<>(headers(tenant, token));
    String url = okapi.getUrl() + "/addresstypes?limit=99";
    ResponseEntity<AddresstypeCollection> response = restTemplate.exchange(url, HttpMethod.GET, entity, AddresstypeCollection.class);
    log.debug("fetch address types: {} milliseconds", TimingUtility.getDeltaInMilliseconds(startTime));
    if (response.getStatusCodeValue() == 200) {
      return response.getBody();
    }
    log.error("Failed to fetch address types: " + response.getStatusCodeValue());
    throw new RuntimeException("Failed to fetch address types: " + response.getStatusCodeValue());
  }

  public void updateRules(JsonNode rules, String path, String tenant, String token) {
    long startTime = System.nanoTime();
    HttpEntity<?> entity = new HttpEntity<>(rules, headers(tenant, token));
    String url = okapi.getUrl() + "/" + path;
    ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.PUT, entity, Void.class);
    log.debug("update rules: {} milliseconds", TimingUtility.getDeltaInMilliseconds(startTime));
    if (response.getStatusCodeValue() < 200 || response.getStatusCodeValue() > 204) {
      log.error("Failed to update rules: " + response.getStatusCodeValue());
      throw new RuntimeException("Failed to update rules: " + response.getStatusCodeValue());  
    }
  }

  public JsonObject fetchRules(String tenant, String token) {
    long startTime = System.nanoTime();
    HttpEntity<?> entity = new HttpEntity<>(headers(tenant, token));
    String url = okapi.getUrl() + "/mapping-rules";
    ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
    log.debug("fetch rules: {} milliseconds", TimingUtility.getDeltaInMilliseconds(startTime));
    if (response.getStatusCodeValue() == 200) {
      return new JsonObject(response.getBody());
    }
    log.error("Failed to fetch rules: " + response.getStatusCodeValue());
    throw new RuntimeException("Failed to fetch rules: " + response.getStatusCodeValue());
  }

  public void updateHridSettings(JsonObject hridSettings, String tenant, String token) {
    long startTime = System.nanoTime();
    HttpEntity<?> entity = new HttpEntity<>(hridSettings.getMap(), headers(tenant, token));
    String url = okapi.getUrl() + "/hrid-settings-storage/hrid-settings";
    ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.PUT, entity, Void.class);
    log.debug("update hrid settings: {} milliseconds", TimingUtility.getDeltaInMilliseconds(startTime));
    if (response.getStatusCodeValue() < 200 || response.getStatusCodeValue() > 204) {
      log.error("Failed to update hrid settings: " + response.getStatusCodeValue());
      throw new RuntimeException("Failed to update hrid settings: " + response.getStatusCodeValue());
    }
  }

  public JsonObject fetchHridSettings(String tenant, String token) {
    long startTime = System.nanoTime();
    HttpEntity<?> entity = new HttpEntity<>(headers(tenant, token));
    String url = okapi.getUrl() + "/hrid-settings-storage/hrid-settings";
    ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
    log.debug("fetch hrid settings: {} milliseconds", TimingUtility.getDeltaInMilliseconds(startTime));
    if (response.getStatusCodeValue() == 200) {
      return new JsonObject(response.getBody());
    }
    log.error("Failed to fetch hrid settings: " + response.getStatusCodeValue());
    throw new RuntimeException("Failed to fetch hrid settings: " + response.getStatusCodeValue());
  }

  public Statisticalcodes fetchStatisticalCodes(String tenant, String token) {
    long startTime = System.nanoTime();
    HttpEntity<?> entity = new HttpEntity<>(headers(tenant, token));
    String url = okapi.getUrl() + "/statistical-codes?limit=999";
    ResponseEntity<Statisticalcodes> response = restTemplate.exchange(url, HttpMethod.GET, entity, Statisticalcodes.class);
    log.debug("fetch statistical codes: {} milliseconds", TimingUtility.getDeltaInMilliseconds(startTime));
    if (response.getStatusCodeValue() == 200) {
      return response.getBody();
    }
    log.error("Failed to fetch statistical codes: " + response.getStatusCodeValue());
    throw new RuntimeException("Failed to fetch statistical codes: " + response.getStatusCodeValue());
  }

  public Materialtypes fetchMaterialtypes(String tenant, String token) {
    long startTime = System.nanoTime();
    HttpEntity<?> entity = new HttpEntity<>(headers(tenant, token));
    String url = okapi.getUrl() + "/material-types?limit=999";
    ResponseEntity<Materialtypes> response = restTemplate.exchange(url, HttpMethod.GET, entity, Materialtypes.class);
    log.debug("fetch material types: {} milliseconds", TimingUtility.getDeltaInMilliseconds(startTime));
    if (response.getStatusCodeValue() == 200) {
      return response.getBody();
    }
    log.error("Failed to fetch material types: " + response.getStatusCodeValue());
    throw new RuntimeException("Failed to fetch material types: " + response.getStatusCodeValue());
  }

  public JobProfile getOrCreateJobProfile(String tenant, String token, JobProfile jobProfile) {
    long startTime = System.nanoTime();
    HttpEntity<?> entity = new HttpEntity<>(headers(tenant, token));
    String url = String.format("%s/data-import-profiles/jobProfiles?query=name='%s'", okapi.getUrl(), jobProfile.getName());
    ResponseEntity<JobProfileCollection> response = restTemplate.exchange(url, HttpMethod.GET, entity, JobProfileCollection.class);
    log.debug("fetch statistical codes: {} milliseconds", TimingUtility.getDeltaInMilliseconds(startTime));
    if (response.getStatusCodeValue() == 200) {
      JobProfileCollection jobProfileCollection = response.getBody();
      if (jobProfileCollection.getTotalRecords() > 0) {
        return jobProfileCollection.getJobProfiles().get(0);
      } else {
        JobProfileUpdateDto jobProfileUpdateDto = new JobProfileUpdateDto();
        jobProfileUpdateDto.setProfile(jobProfile);
        return createJobProfile(tenant, token, jobProfileUpdateDto);
      }
    }
    log.error("Failed to fetch statistical codes: " + response.getStatusCodeValue());
    throw new RuntimeException("Failed to fetch statistical codes: " + response.getStatusCodeValue());
  }

  public JobProfile createJobProfile(String tenant, String token, JobProfileUpdateDto jobProfileUpdateDto) {
    long startTime = System.nanoTime();
    HttpEntity<JobProfileUpdateDto> entity = new HttpEntity<>(jobProfileUpdateDto, headers(tenant, token));
    String url = okapi.getUrl() + "/data-import-profiles/jobProfiles";
    ResponseEntity<JobProfileUpdateDto> response = restTemplate.exchange(url, HttpMethod.POST, entity, JobProfileUpdateDto.class);
    log.debug("create job profile: {} milliseconds", TimingUtility.getDeltaInMilliseconds(startTime));
    if (response.getStatusCodeValue() == 201) {
      return DatabindCodec.mapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
          .convertValue(response.getBody().getProfile(), JobProfile.class);
    }
    log.error("Failed to create job profile: " + response.getStatusCodeValue());
    throw new RuntimeException("Failed to create job profile: " + response.getStatusCodeValue());
  }

  public InitJobExecutionsRsDto createJobExecution(String tenant, String token, InitJobExecutionsRqDto jobExecutionDto) {
    long startTime = System.nanoTime();
    HttpEntity<InitJobExecutionsRqDto> entity = new HttpEntity<>(jobExecutionDto, headers(tenant, token));
    String url = okapi.getUrl() + "/change-manager/jobExecutions";
    ResponseEntity<InitJobExecutionsRsDto> response = restTemplate.exchange(url, HttpMethod.POST, entity, InitJobExecutionsRsDto.class);
    log.debug("create job execution: {} milliseconds", TimingUtility.getDeltaInMilliseconds(startTime));
    if (response.getStatusCodeValue() == 201) {
      return response.getBody();
    }
    log.error("Failed to create job execution: " + response.getStatusCodeValue());
    throw new RuntimeException("Failed to create job execution: " + response.getStatusCodeValue());
  }

  public void finishJobExecution(String tenant, String token, String jobExecutionId,  RawRecordsDto rawRecordsDto) {
    postJobExecutionRecords(tenant, token, jobExecutionId, rawRecordsDto);
    JobExecution jobExecution = getJobExecution(tenant, token, jobExecutionId);
    jobExecution.setCompletedDate(new Date());
    jobExecution.setStatus(Status.COMMITTED);
    jobExecution.setUiStatus(UiStatus.RUNNING_COMPLETE);
    jobExecution.getProgress().setCurrent(rawRecordsDto.getRecordsMetadata().getCounter());
    long startTime = System.nanoTime();
    HttpEntity<JobExecution> entity = new HttpEntity<>(jobExecution, headers(tenant, token));
    String url = okapi.getUrl() + "/change-manager/jobExecutions/" + jobExecutionId;
    ResponseEntity<JobExecution> response = restTemplate.exchange(url, HttpMethod.PUT, entity, JobExecution.class);
    log.debug("finish job execution: {} milliseconds", TimingUtility.getDeltaInMilliseconds(startTime));
    if (response.getStatusCodeValue() == 200) {
      return;
    }
    log.error("Failed to finish job execution: " + response.getStatusCodeValue());
    throw new RuntimeException("Failed to finish job execution: " + response.getStatusCodeValue());
  }

  public void postJobExecutionRecords(String tenant, String token, String jobExecutionId, RawRecordsDto rawRecordsDto) {
    long startTime = System.nanoTime();
    HttpEntity<RawRecordsDto> entity = new HttpEntity<>(rawRecordsDto, headers(tenant, token));
    String url = okapi.getUrl() + "/change-manager/jobExecutions/" + jobExecutionId + "/records";
    ResponseEntity<JobExecution> response = restTemplate.exchange(url, HttpMethod.POST, entity, JobExecution.class);
    log.debug("update job execution records: {} milliseconds", TimingUtility.getDeltaInMilliseconds(startTime));
    if (response.getStatusCodeValue() == 204) {
      return;
    }
    log.error("Failed to update job execution: " + response.getStatusCodeValue());
    throw new RuntimeException("Failed to update job execution: " + response.getStatusCodeValue());
  }

  public JobExecution getJobExecution(String tenant, String token, String jobExecutionId) {
    long startTime = System.nanoTime();
    HttpEntity<?> entity = new HttpEntity<>(headers(tenant, token));
    String url = okapi.getUrl() + "/change-manager/jobExecutions/" + jobExecutionId;
    ResponseEntity<JobExecution> response = restTemplate.exchange(url, HttpMethod.GET, entity, JobExecution.class);
    log.debug("fetch job execution: {} milliseconds", TimingUtility.getDeltaInMilliseconds(startTime));
    if (response.getStatusCodeValue() == 200) {
      return response.getBody();
    }
    log.error("Failed to fetch job execution: " + response.getStatusCodeValue());
    throw new RuntimeException("Failed to fetch job execution: " + response.getStatusCodeValue());
  }

  public Locations fetchLocations(String tenant, String token) {
    long startTime = System.nanoTime();
    HttpEntity<?> entity = new HttpEntity<>(headers(tenant, token));
    String url = okapi.getUrl() + "/locations?limit=9999";
    ResponseEntity<Locations> response = restTemplate.exchange(url, HttpMethod.GET, entity, Locations.class);
    log.debug("fetch locations: {} milliseconds", TimingUtility.getDeltaInMilliseconds(startTime));
    if (response.getStatusCodeValue() == 200) {
      return response.getBody();
    }
    log.error("Failed to fetch locations: " + response.getStatusCodeValue());
    throw new RuntimeException("Failed to fetch locations: " + response.getStatusCodeValue());
  }

  public Loantypes fetchLoanTypes(String tenant, String token) {
    long startTime = System.nanoTime();
    HttpEntity<?> entity = new HttpEntity<>(headers(tenant, token));
    String url = okapi.getUrl() + "/loan-types?limit=999";
    ResponseEntity<Loantypes> response = restTemplate.exchange(url, HttpMethod.GET, entity, Loantypes.class);
    log.debug("fetch loan types: {} milliseconds", TimingUtility.getDeltaInMilliseconds(startTime));
    if (response.getStatusCodeValue() == 200) {
      return response.getBody();
    }
    log.error("Failed to fetch loan types: " + response.getStatusCodeValue());
    throw new RuntimeException("Failed to fetch loan types: " + response.getStatusCodeValue());
  }

  public Items fetchItemRecordsByHoldingsRecordId(String tenant, String token, String holdingsRecordId) {
    long startTime = System.nanoTime();
    HttpEntity<?> entity = new HttpEntity<>(headers(tenant, token));
    String url = okapi.getUrl() + "/item-storage/items?query=holdingsRecordId==" + holdingsRecordId;
    ResponseEntity<Items> response = restTemplate.exchange(url, HttpMethod.GET, entity, Items.class);
    log.debug("fetch item records: {} milliseconds", TimingUtility.getDeltaInMilliseconds(startTime));
    if (response.getStatusCodeValue() == 200) {
      return response.getBody();
    }
    log.error("Failed to fetch item records: " + response.getStatusCodeValue());
    throw new RuntimeException("Failed to fetch item records: " + response.getStatusCodeValue());
  }

  public Instance postInstance(String tenant, String token, Instance instance) {
    long startTime = System.nanoTime();
    HttpEntity<Instance> entity = new HttpEntity<>(instance, headers(tenant, token));
    String url = okapi.getUrl() + "/instance-storage/instances";
    ResponseEntity<Instance> response = restTemplate.exchange(url, HttpMethod.POST, entity, Instance.class);
    log.debug("create instance: {} milliseconds", TimingUtility.getDeltaInMilliseconds(startTime));
    if (response.getStatusCodeValue() == 201) {
      return response.getBody();
    }
    log.error("Failed to create instance: " + response.getStatusCodeValue());
    throw new RuntimeException("Failed to create instance: " + response.getStatusCodeValue());
  }

  public Instancerelationship postInstancerelationship(String tenant, String token, Instancerelationship holdingsrecord) {
    long startTime = System.nanoTime();
    HttpEntity<Instancerelationship> entity = new HttpEntity<>(holdingsrecord, headers(tenant, token));
    String url = okapi.getUrl() + "/instance-storage/instance-relationships";
    ResponseEntity<Instancerelationship> response = restTemplate.exchange(url, HttpMethod.POST, entity, Instancerelationship.class);
    log.debug("create instance relationships: {} milliseconds", TimingUtility.getDeltaInMilliseconds(startTime));
    if (response.getStatusCodeValue() == 201) {
      return response.getBody();
    }
    log.error("Failed to create instance relationships: " + response.getStatusCodeValue());
    throw new RuntimeException("Failed to create instance relationships: " + response.getStatusCodeValue());
  }

  public Holdingsrecord fetchHoldingsRecordById(String tenant, String token, String id) {
    long startTime = System.nanoTime();
    HttpEntity<?> entity = new HttpEntity<>(headers(tenant, token));
    String url = okapi.getUrl() + "/holdings-storage/holdings/" + id;
    ResponseEntity<Holdingsrecord> response = restTemplate.exchange(url, HttpMethod.GET, entity, Holdingsrecord.class);
    log.debug("fetch holdings record: {} milliseconds", TimingUtility.getDeltaInMilliseconds(startTime));
    if (response.getStatusCodeValue() == 200) {
      return response.getBody();
    }
    log.error("Failed to fetch holdings record: " + response.getStatusCodeValue());
    throw new RuntimeException("Failed to fetch holdings record: " + response.getStatusCodeValue());
  }

  public Holdingsrecord postHoldingsrecord(String tenant, String token, Holdingsrecord holdingsrecord) {
    long startTime = System.nanoTime();
    HttpEntity<Holdingsrecord> entity = new HttpEntity<>(holdingsrecord, headers(tenant, token));
    String url = okapi.getUrl() + "/holdings-storage/holdings";
    ResponseEntity<Holdingsrecord> response = restTemplate.exchange(url, HttpMethod.POST, entity, Holdingsrecord.class);
    log.debug("create holdings record: {} milliseconds", TimingUtility.getDeltaInMilliseconds(startTime));
    if (response.getStatusCodeValue() == 201) {
      return response.getBody();
    }
    log.error("Failed to create holdings record: " + response.getStatusCodeValue());
    throw new RuntimeException("Failed to create holdings record: " + response.getStatusCodeValue());
  }

  public void putItem(String tenant, String token, Item item) {
    long startTime = System.nanoTime();
    HttpEntity<Item> entity = new HttpEntity<>(item, headers(tenant, token));
    String url = okapi.getUrl() + "/item-storage/items/" + item.getId();
    ResponseEntity<Item> response = restTemplate.exchange(url, HttpMethod.PUT, entity, Item.class);
    log.debug("Update item: {} milliseconds", TimingUtility.getDeltaInMilliseconds(startTime));
    if (response.getStatusCodeValue() == 204) {
      return;
    }
    log.error("Failed to update item: " + response.getStatusCodeValue());
    throw new RuntimeException("Failed to update item: " + response.getStatusCodeValue());
  }

  public ImportResponse postUserdataimportCollection(String tenant, String token, UserdataimportCollection userdataimportCollection) {
    long startTime = System.nanoTime();
    HttpEntity<UserdataimportCollection> entity = new HttpEntity<>(userdataimportCollection, headers(tenant, token));
    String url = okapi.getUrl() + "/user-import";
    ResponseEntity<ImportResponse> response = restTemplate.exchange(url, HttpMethod.POST, entity, ImportResponse.class);
    log.debug("importing users: {} milliseconds", TimingUtility.getDeltaInMilliseconds(startTime));
    if (response.getStatusCodeValue() == 200) {
      return response.getBody();
    }
    log.error("Failed to import users: " + response.getStatusCodeValue());
    throw new RuntimeException("Failed to import users: " + response.getStatusCodeValue());
  }

  public MappingParameters getMappingParamaters(String tenant, String token) {
    long startTime = System.nanoTime();
    final MappingParameters mappingParameters = new MappingParameters();
    // @formatter:off
    Arrays.asList(new ReferenceFetcher[] {
      new ReferenceFetcher("/identifier-types?limit=" + SETTING_LIMIT, Identifiertypes.class, "identifierTypes"),
      new ReferenceFetcher("/classification-types?limit=" + SETTING_LIMIT, Classificationtypes.class, "classificationTypes"),
      new ReferenceFetcher("/instance-types?limit=" + SETTING_LIMIT, Instancetypes.class, "instanceTypes"),
      new ReferenceFetcher("/electronic-access-relationships?limit=" + SETTING_LIMIT, Electronicaccessrelationships.class, "electronicAccessRelationships"),
      new ReferenceFetcher("/instance-formats?limit=" + SETTING_LIMIT, Instanceformats.class, "instanceFormats"),
      new ReferenceFetcher("/contributor-types?limit=" + SETTING_LIMIT, Contributortypes.class, "contributorTypes"),
      new ReferenceFetcher("/contributor-name-types?limit=" + SETTING_LIMIT, Contributornametypes.class, "contributorNameTypes"),
      new ReferenceFetcher("/instance-note-types?limit=" + SETTING_LIMIT, Instancenotetypes.class, "instanceNoteTypes"),
      new ReferenceFetcher("/alternative-title-types?limit=" + SETTING_LIMIT, Alternativetitletypes.class, "alternativeTitleTypes"),
      new ReferenceFetcher("/modes-of-issuance?limit=" + SETTING_LIMIT, Issuancemodes.class, "issuanceModes")
    }).forEach(fetcher -> {
      HttpEntity<Credentials> entity = new HttpEntity<Credentials>(headers(tenant, token));
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
        log.error(e.getMessage());
        throw new RuntimeException(e);
      }
    });
    mappingParameters.setInitialized(true);
    // @formatter:on
    log.debug("get mapping parameters: {} milliseconds", TimingUtility.getDeltaInMilliseconds(startTime));
    return mappingParameters;
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
