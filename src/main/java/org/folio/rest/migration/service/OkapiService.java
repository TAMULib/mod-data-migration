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
import org.folio.rest.jaxrs.model.circulation.Loan;
import org.folio.rest.jaxrs.model.dataimport.raml_storage.schemas.common.Status;
import org.folio.rest.jaxrs.model.dataimport.raml_storage.schemas.dto.InitJobExecutionsRqDto;
import org.folio.rest.jaxrs.model.dataimport.raml_storage.schemas.dto.InitJobExecutionsRsDto;
import org.folio.rest.jaxrs.model.dataimport.raml_storage.schemas.dto.JobExecution;
import org.folio.rest.jaxrs.model.dataimport.raml_storage.schemas.dto.JobExecution.UiStatus;
import org.folio.rest.jaxrs.model.dataimport.raml_storage.schemas.dto.Progress;
import org.folio.rest.jaxrs.model.dataimport.raml_storage.schemas.dto.RawRecordsDto;
import org.folio.rest.jaxrs.model.dataimport.raml_storage.schemas.mod_data_import_converter_storage.JobProfile;
import org.folio.rest.jaxrs.model.dataimport.raml_storage.schemas.mod_data_import_converter_storage.JobProfileCollection;
import org.folio.rest.jaxrs.model.dataimport.raml_storage.schemas.mod_data_import_converter_storage.JobProfileUpdateDto;
import org.folio.rest.jaxrs.model.feesfines.Accountdata;
import org.folio.rest.jaxrs.model.feesfines.actions.Feefineactiondata;
import org.folio.rest.jaxrs.model.inventory.Holdingsrecord;
import org.folio.rest.jaxrs.model.inventory.Holdingsrecords;
import org.folio.rest.jaxrs.model.inventory.HoldingsrecordsPost;
import org.folio.rest.jaxrs.model.inventory.Instance;
import org.folio.rest.jaxrs.model.inventory.Instancerelationship;
import org.folio.rest.jaxrs.model.inventory.Item;
import org.folio.rest.jaxrs.model.inventory.Items;
import org.folio.rest.jaxrs.model.inventory.Loantypes;
import org.folio.rest.jaxrs.model.inventory.Locations;
import org.folio.rest.jaxrs.model.inventory.Materialtypes;
import org.folio.rest.jaxrs.model.inventory.Servicepoints;
import org.folio.rest.jaxrs.model.inventory.Statisticalcodes;
import org.folio.rest.jaxrs.model.notes.types.notes.Note;
import org.folio.rest.jaxrs.model.orders.acq_models.mod_finance.schemas.FundCollection;
import org.folio.rest.jaxrs.model.orders.acq_models.mod_orders.schemas.CompositePurchaseOrder;
import org.folio.rest.jaxrs.model.organizations.acq_models.mod_orgs.schemas.Contact;
import org.folio.rest.jaxrs.model.organizations.acq_models.mod_orgs.schemas.Organization;
import org.folio.rest.jaxrs.model.userimport.schemas.ImportResponse;
import org.folio.rest.jaxrs.model.userimport.schemas.UserdataimportCollection;
import org.folio.rest.jaxrs.model.users.AddresstypeCollection;
import org.folio.rest.jaxrs.model.users.Proxyfor;
import org.folio.rest.jaxrs.model.users.Userdata;
import org.folio.rest.jaxrs.model.users.UserdataCollection;
import org.folio.rest.jaxrs.model.users.Usergroups;
import org.folio.rest.migration.config.model.Credentials;
import org.folio.rest.migration.config.model.Okapi;
import org.folio.rest.migration.model.ReferenceData;
import org.folio.rest.migration.model.ReferenceDatum;
import org.folio.rest.migration.model.request.ExternalOkapi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
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
    String url = okapi.getUrl() + "/authn/login";
    HttpEntity<Credentials> entity = new HttpEntity<>(okapi.getCredentials(), headers(tenant));
    ResponseEntity<Credentials> response = restTemplate.exchange(url, HttpMethod.POST, entity, Credentials.class);
    return response.getHeaders().getFirst("X-Okapi-Token");
  }

  public String getToken(ExternalOkapi okapi) {
    String url = okapi.getUrl() + "/authn/login";
    HttpEntity<Credentials> entity = new HttpEntity<>(okapi.getCredentials(), headers(okapi.getTenant()));
    ResponseEntity<Credentials> response = restTemplate.exchange(url, HttpMethod.POST, entity, Credentials.class);
    return response.getHeaders().getFirst("X-Okapi-Token");
  }

  public Servicepoints fetchServicepoints(ExternalOkapi okapi, String token) {
    HttpEntity<?> entity = new HttpEntity<>(headers(okapi.getTenant(), token));
    String url = okapi.getUrl() + "/service-points?limit=9999";
    ResponseEntity<Servicepoints> response = restTemplate.exchange(url, HttpMethod.GET, entity, Servicepoints.class);
    return response.getBody();
  }

  public JsonNode fetchCalendarPeriodsForServicepoint(ExternalOkapi okapi, String token, String servicePointId) {
    HttpEntity<?> entity = new HttpEntity<>(headers(okapi.getTenant(), token));
    String url = okapi.getUrl() + "/calendar/periods/" + servicePointId + "/period?withOpeningDays=true&showPast=true";
    ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.GET, entity, JsonNode.class);
    return response.getBody();
  }

  public JsonNode fetchReferenceData(ExternalOkapi okapi, ReferenceData datum) {
    String url = okapi.getUrl() + datum.getPath() + "?" + datum.getQuery();
    HttpEntity<JsonNode> entity = new HttpEntity<>(headers(datum.getTenant(), datum.getToken()));
    ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.GET, entity, JsonNode.class);
    return response.getBody();
  }

  public JsonNode fetchReferenceDataById(ExternalOkapi okapi, ReferenceData datum, String id) {
    String url = okapi.getUrl() + datum.getPath() + "/" + id;
    HttpEntity<JsonNode> entity = new HttpEntity<>(headers(datum.getTenant(), datum.getToken()));
    ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.GET, entity, JsonNode.class);
    return response.getBody();
  }

  public JsonNode createReferenceData(ReferenceDatum referenceDatum) {
    String url = okapi.getUrl() + referenceDatum.getPath();
    HttpEntity<JsonNode> entity = new HttpEntity<>(referenceDatum.getData(), headers(referenceDatum.getTenant(), referenceDatum.getToken()));
    ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.POST, entity, JsonNode.class);
    return response.getBody();
  }

  public Note createNote(Note note, String tenant, String token) {
    String url = okapi.getUrl() + "/notes";
    HttpEntity<Note> entity = new HttpEntity<>(note, headers(tenant, token));
    ResponseEntity<Note> response = restTemplate.exchange(url, HttpMethod.POST, entity, Note.class);
    return response.getBody();
  }

  public Contact createContact(Contact contact, String tenant, String token) {
    String url = okapi.getUrl() + "/organizations-storage/contacts";
    HttpEntity<Contact> entity = new HttpEntity<>(contact, headers(tenant, token));
    ResponseEntity<Contact> response = restTemplate.exchange(url, HttpMethod.POST, entity, Contact.class);
    return response.getBody();
  }

  public Organization createOrganization(Organization organization, String tenant, String token) {
    String url = okapi.getUrl() + "/organizations-storage/organizations";
    HttpEntity<Organization> entity = new HttpEntity<>(organization, headers(tenant, token));
    ResponseEntity<Organization> response = restTemplate.exchange(url, HttpMethod.POST, entity, Organization.class);
    return response.getBody();
  }

  public JsonNode createRequest(JsonNode request, String tenant, String token) {
    String url = okapi.getUrl() + "/circulation/requests";
    HttpEntity<JsonNode> entity = new HttpEntity<>(request, headers(tenant, token));
    ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.POST, entity, JsonNode.class);
    return response.getBody();
  }

  public Proxyfor createProxyFor(Proxyfor account, String tenant, String token) {
    String url = okapi.getUrl() + "/proxiesfor";
    HttpEntity<Proxyfor> entity = new HttpEntity<>(account, headers(tenant, token));
    ResponseEntity<Proxyfor> response = restTemplate.exchange(url, HttpMethod.POST, entity, Proxyfor.class);
    return response.getBody();
  }

  public Accountdata createAccount(Accountdata account, String tenant, String token) {
    String url = okapi.getUrl() + "/accounts";
    HttpEntity<Accountdata> entity = new HttpEntity<>(account, headers(tenant, token));
    ResponseEntity<Accountdata> response = restTemplate.exchange(url, HttpMethod.POST, entity, Accountdata.class);
    return response.getBody();
  }

  public Feefineactiondata createFeeFineAction(Feefineactiondata feefineaction, String tenant, String token) {
    String url = okapi.getUrl() + "/feefineactions";
    HttpEntity<Feefineactiondata> entity = new HttpEntity<>(feefineaction, headers(tenant, token));
    ResponseEntity<Feefineactiondata> response = restTemplate.exchange(url, HttpMethod.POST, entity, Feefineactiondata.class);
    return response.getBody();
  }

  public Loan checkoutByBarcode(JsonNode request, String tenant, String token) {
    String url = okapi.getUrl() + "/circulation/check-out-by-barcode";
    HttpEntity<JsonNode> entity = new HttpEntity<>(request, headers(tenant, token));
    ResponseEntity<Loan> response = restTemplate.exchange(url, HttpMethod.POST, entity, Loan.class);
    return response.getBody();
  }

  public void updateLoan(JsonNode loan, String tenant, String token) {
    String url = okapi.getUrl() + "/circulation/loans/" + loan.get("id").asText();
    HttpEntity<?> entity = new HttpEntity<>(loan, headers(tenant, token));
    restTemplate.exchange(url, HttpMethod.PUT, entity, Void.class);
  }

  public Servicepoints fetchServicepoints(String tenant, String token) {
    HttpEntity<?> entity = new HttpEntity<>(headers(tenant, token));
    String url = okapi.getUrl() + "/service-points?limit=9999";
    ResponseEntity<Servicepoints> response = restTemplate.exchange(url, HttpMethod.GET, entity, Servicepoints.class);
    return response.getBody();
  }

  public Userdata lookupUserByUsername(String tenant, String token, String username) {
    HttpEntity<?> entity = new HttpEntity<>(headers(tenant, token));
    String url = okapi.getUrl() + "/users?query=username==" + username;
    ResponseEntity<UserdataCollection> response = restTemplate.exchange(url, HttpMethod.GET, entity, UserdataCollection.class);
    UserdataCollection userCollection = response.getBody();
    if (userCollection.getTotalRecords() > 0) {
      return userCollection.getUsers().get(0);
    }
    throw new RuntimeException("User with username " + username + " not found");
  }

  public Userdata lookupUserById(String tenant, String token, String id) {
    HttpEntity<?> entity = new HttpEntity<>(headers(tenant, token));
    String url = okapi.getUrl() + "/users/" + id;
    ResponseEntity<Userdata> response = restTemplate.exchange(url, HttpMethod.GET, entity, Userdata.class);
    return response.getBody();
  }

  public Usergroups fetchUsergroups(String tenant, String token) {
    HttpEntity<?> entity = new HttpEntity<>(headers(tenant, token));
    String url = okapi.getUrl() + "/groups?limit=9999";
    ResponseEntity<Usergroups> response = restTemplate.exchange(url, HttpMethod.GET, entity, Usergroups.class);
    return response.getBody();
  }

  public AddresstypeCollection fetchAddresstypes(String tenant, String token) {
    HttpEntity<?> entity = new HttpEntity<>(headers(tenant, token));
    String url = okapi.getUrl() + "/addresstypes?limit=99";
    ResponseEntity<AddresstypeCollection> response = restTemplate.exchange(url, HttpMethod.GET, entity, AddresstypeCollection.class);
    return response.getBody();
  }

  public void updateRules(JsonNode rules, String path, String tenant, String token) {
    HttpEntity<?> entity = new HttpEntity<>(rules, headers(tenant, token));
    String url = okapi.getUrl() + "/" + path;
    restTemplate.exchange(url, HttpMethod.PUT, entity, Void.class);
  }

  public JsonObject fetchRules(String tenant, String token) {
    HttpEntity<?> entity = new HttpEntity<>(headers(tenant, token));
    String url = okapi.getUrl() + "/mapping-rules";
    ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
    return new JsonObject(response.getBody());
  }

  public void updateHridSettings(JsonObject hridSettings, String tenant, String token) {
    HttpEntity<?> entity = new HttpEntity<>(hridSettings.getMap(), headers(tenant, token));
    String url = okapi.getUrl() + "/hrid-settings-storage/hrid-settings";
    restTemplate.exchange(url, HttpMethod.PUT, entity, Void.class);
  }

  public JsonObject fetchHridSettings(String tenant, String token) {
    HttpEntity<?> entity = new HttpEntity<>(headers(tenant, token));
    String url = okapi.getUrl() + "/hrid-settings-storage/hrid-settings";
    ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
    return new JsonObject(response.getBody());
  }

  public Statisticalcodes fetchStatisticalCodes(String tenant, String token) {
    HttpEntity<?> entity = new HttpEntity<>(headers(tenant, token));
    String url = okapi.getUrl() + "/statistical-codes?limit=999";
    ResponseEntity<Statisticalcodes> response = restTemplate.exchange(url, HttpMethod.GET, entity, Statisticalcodes.class);
    return response.getBody();
  }

  public Materialtypes fetchMaterialtypes(String tenant, String token) {
    HttpEntity<?> entity = new HttpEntity<>(headers(tenant, token));
    String url = okapi.getUrl() + "/material-types?limit=999";
    ResponseEntity<Materialtypes> response = restTemplate.exchange(url, HttpMethod.GET, entity, Materialtypes.class);
    return response.getBody();
  }

  public FundCollection fetchFunds(String tenant, String token) {
    HttpEntity<?> entity = new HttpEntity<>(headers(tenant, token));
    String url = okapi.getUrl() + "/finance/funds?limit=999";
    ResponseEntity<FundCollection> response = restTemplate.exchange(url, HttpMethod.GET, entity, FundCollection.class);
    return response.getBody();
  }

  public JobProfile getOrCreateJobProfile(String tenant, String token, JobProfile jobProfile) {
    HttpEntity<?> entity = new HttpEntity<>(headers(tenant, token));
    String url = String.format("%s/data-import-profiles/jobProfiles?query=name='%s'", okapi.getUrl(), jobProfile.getName());
    ResponseEntity<JobProfileCollection> response = restTemplate.exchange(url, HttpMethod.GET, entity, JobProfileCollection.class);
    JobProfileCollection jobProfileCollection = response.getBody();
    if (jobProfileCollection.getTotalRecords() > 0) {
      return jobProfileCollection.getJobProfiles().get(0);
    } else {
      JobProfileUpdateDto jobProfileUpdateDto = new JobProfileUpdateDto();
      jobProfileUpdateDto.setProfile(jobProfile);
      return createJobProfile(tenant, token, jobProfileUpdateDto);
    }
  }

  public JobProfile createJobProfile(String tenant, String token, JobProfileUpdateDto jobProfileUpdateDto) {
    HttpEntity<JobProfileUpdateDto> entity = new HttpEntity<>(jobProfileUpdateDto, headers(tenant, token));
    String url = okapi.getUrl() + "/data-import-profiles/jobProfiles";
    ResponseEntity<JobProfileUpdateDto> response = restTemplate.exchange(url, HttpMethod.POST, entity, JobProfileUpdateDto.class);
    return DatabindCodec.mapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
      .convertValue(response.getBody().getProfile(), JobProfile.class);
  }

  public InitJobExecutionsRsDto createJobExecution(String tenant, String token, InitJobExecutionsRqDto jobExecutionDto) {
    HttpEntity<InitJobExecutionsRqDto> entity = new HttpEntity<>(jobExecutionDto, headers(tenant, token));
    String url = okapi.getUrl() + "/change-manager/jobExecutions";
    ResponseEntity<InitJobExecutionsRsDto> response = restTemplate.exchange(url, HttpMethod.POST, entity, InitJobExecutionsRsDto.class);
    return response.getBody();
  }

  public void finishJobExecution(String tenant, String token, String jobExecutionId, RawRecordsDto rawRecordsDto) {
    postJobExecutionRecords(tenant, token, jobExecutionId, rawRecordsDto);
    JobExecution jobExecution = getJobExecution(tenant, token, jobExecutionId);
    jobExecution.setCompletedDate(new Date());
    jobExecution.setStatus(Status.COMMITTED);
    jobExecution.setUiStatus(UiStatus.RUNNING_COMPLETE);
    Progress progress = new Progress();
    progress.setCurrent(rawRecordsDto.getRecordsMetadata().getCounter());
    progress.setTotal(rawRecordsDto.getRecordsMetadata().getTotal());
    progress.setJobExecutionId(jobExecution.getId());
    jobExecution.setProgress(progress);
    putJobExecution(tenant, token, jobExecution);
  }

  public void postJobExecutionRecords(String tenant, String token, String jobExecutionId, RawRecordsDto rawRecordsDto) {
    HttpEntity<RawRecordsDto> entity = new HttpEntity<>(rawRecordsDto, headers(tenant, token));
    String url = okapi.getUrl() + "/change-manager/jobExecutions/" + jobExecutionId + "/records";
    restTemplate.exchange(url, HttpMethod.POST, entity, JobExecution.class);
  }

  public JobExecution getJobExecution(String tenant, String token, String jobExecutionId) {
    HttpEntity<?> entity = new HttpEntity<>(headers(tenant, token));
    String url = okapi.getUrl() + "/change-manager/jobExecutions/" + jobExecutionId;
    ResponseEntity<JobExecution> response = restTemplate.exchange(url, HttpMethod.GET, entity, JobExecution.class);
    return response.getBody();
  }

  public JobExecution putJobExecution(String tenant, String token, JobExecution jobExecution) {
    HttpEntity<JobExecution> entity = new HttpEntity<>(jobExecution, headers(tenant, token));
    String url = okapi.getUrl() + "/change-manager/jobExecutions/" + jobExecution.getId();
    ResponseEntity<JobExecution> response = restTemplate.exchange(url, HttpMethod.PUT, entity, JobExecution.class);
    return response.getBody();
  }

  public Locations fetchLocations(String tenant, String token) {
    HttpEntity<?> entity = new HttpEntity<>(headers(tenant, token));
    String url = okapi.getUrl() + "/locations?limit=9999";
    ResponseEntity<Locations> response = restTemplate.exchange(url, HttpMethod.GET, entity, Locations.class);
    return response.getBody();
  }

  public Loantypes fetchLoanTypes(String tenant, String token) {
    HttpEntity<?> entity = new HttpEntity<>(headers(tenant, token));
    String url = okapi.getUrl() + "/loan-types?limit=999";
    ResponseEntity<Loantypes> response = restTemplate.exchange(url, HttpMethod.GET, entity, Loantypes.class);
    return response.getBody();
  }

  public Holdingsrecords fetchHoldingsRecordsByIdAndInstanceId(String tenant, String token, String id, String instanceId) {
    HttpEntity<?> entity = new HttpEntity<>(headers(tenant, token));
    String url = okapi.getUrl() + "/holdings-storage/holdings?query=(id==" + id + " AND instanceId==" + instanceId + ")";
    ResponseEntity<Holdingsrecords> response = restTemplate.exchange(url, HttpMethod.GET, entity, Holdingsrecords.class);
    return response.getBody();
  }

  public Items fetchItemRecordsByHoldingsRecordId(String tenant, String token, String holdingsRecordId) {
    HttpEntity<?> entity = new HttpEntity<>(headers(tenant, token));
    String url = okapi.getUrl() + "/item-storage/items?query=holdingsRecordId==" + holdingsRecordId;
    ResponseEntity<Items> response = restTemplate.exchange(url, HttpMethod.GET, entity, Items.class);
    return response.getBody();
  }

  public Instance fetchInstanceById(String tenant, String token, String instanceId) {
    HttpEntity<Instance> entity = new HttpEntity<>(headers(tenant, token));
    String url = okapi.getUrl() + "/instance-storage/instances/" + instanceId;
    ResponseEntity<Instance> response = restTemplate.exchange(url, HttpMethod.GET, entity, Instance.class);
    return response.getBody();
  }

  public Instance postInstance(String tenant, String token, Instance instance) {
    HttpEntity<Instance> entity = new HttpEntity<>(instance, headers(tenant, token));
    String url = okapi.getUrl() + "/instance-storage/instances";
    ResponseEntity<Instance> response = restTemplate.exchange(url, HttpMethod.POST, entity, Instance.class);
    return response.getBody();
  }

  public Instancerelationship postInstancerelationship(String tenant, String token, Instancerelationship holdingsrecord) {
    HttpEntity<Instancerelationship> entity = new HttpEntity<>(holdingsrecord, headers(tenant, token));
    String url = okapi.getUrl() + "/instance-storage/instance-relationships";
    ResponseEntity<Instancerelationship> response = restTemplate.exchange(url, HttpMethod.POST, entity, Instancerelationship.class);
    return response.getBody();
  }

  public Holdingsrecord fetchHoldingsRecordById(String tenant, String token, String id) {
    HttpEntity<?> entity = new HttpEntity<>(headers(tenant, token));
    String url = okapi.getUrl() + "/holdings-storage/holdings/" + id;
    ResponseEntity<Holdingsrecord> response = restTemplate.exchange(url, HttpMethod.GET, entity, Holdingsrecord.class);
    return response.getBody();
  }

  public Item fetchItemById(String tenant, String token, String id) {
    HttpEntity<?> entity = new HttpEntity<>(headers(tenant, token));
    String url = okapi.getUrl() + "/item-storage/items/" + id;
    ResponseEntity<Item> response = restTemplate.exchange(url, HttpMethod.GET, entity, Item.class);
    return response.getBody();
  }

  public Holdingsrecord postHoldingsrecord(String tenant, String token, Holdingsrecord holdingsrecord) {
    HttpEntity<Holdingsrecord> entity = new HttpEntity<>(holdingsrecord, headers(tenant, token));
    String url = okapi.getUrl() + "/holdings-storage/holdings";
    ResponseEntity<Holdingsrecord> response = restTemplate.exchange(url, HttpMethod.POST, entity, Holdingsrecord.class);
    return response.getBody();
  }

  public Holdingsrecord putHoldingsrecord(String tenant, String token, Holdingsrecord holdingsrecord) {
    HttpEntity<Holdingsrecord> entity = new HttpEntity<>(holdingsrecord, headers(tenant, token));
    String url = okapi.getUrl() + "/holdings-storage/holdings/" + holdingsrecord.getId();
    ResponseEntity<Holdingsrecord> response = restTemplate.exchange(url, HttpMethod.PUT, entity, Holdingsrecord.class);
    return response.getBody();
  }

  public void putItem(String tenant, String token, Item item) {
    HttpEntity<Item> entity = new HttpEntity<>(item, headers(tenant, token));
    String url = okapi.getUrl() + "/item-storage/items/" + item.getId();
    restTemplate.exchange(url, HttpMethod.PUT, entity, Item.class);
  }

  public ImportResponse postUserdataimportCollection(String tenant, String token, UserdataimportCollection userdataimportCollection) {
    HttpEntity<UserdataimportCollection> entity = new HttpEntity<>(userdataimportCollection, headers(tenant, token));
    String url = okapi.getUrl() + "/user-import";
    ResponseEntity<ImportResponse> response = restTemplate.exchange(url, HttpMethod.POST, entity, ImportResponse.class);
    return response.getBody();
  }

  public void postHoldingsRecordsPostBatch(String tenant, String token, HoldingsrecordsPost holdingsRecordsPost) {
    HttpEntity<HoldingsrecordsPost> entity = new HttpEntity<>(holdingsRecordsPost, headers(tenant, token));
    String url = okapi.getUrl() + "/holdings-storage/batch/synchronous";
    restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
  }

  public CompositePurchaseOrder postCompositePurchaseOrder(String tenant, String token, CompositePurchaseOrder compositePurchaseOrder) {
    HttpEntity<CompositePurchaseOrder> entity = new HttpEntity<>(compositePurchaseOrder, headers(tenant, token));
    String url = okapi.getUrl() + "/orders/composite-orders";
    ResponseEntity<CompositePurchaseOrder> response = restTemplate.exchange(url, HttpMethod.POST, entity, CompositePurchaseOrder.class);
    return response.getBody();
  }

  public MappingParameters getMappingParamaters(String tenant, String token) {
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
      } catch (RestClientException | NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
        log.error(e.getMessage());
        throw new RuntimeException(e);
      }
    });
    mappingParameters.setInitialized(true);
    // @formatter:on
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
