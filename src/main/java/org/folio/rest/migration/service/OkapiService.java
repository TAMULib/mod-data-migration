package org.folio.rest.migration.service;

import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import org.folio.processing.mapping.defaultmapper.processor.parameters.MappingParameters;
import org.folio.rest.jaxrs.model.circulation.Loan;
import org.folio.rest.jaxrs.model.dataimport.raml_storage.schemas.mod_source_record_storage.Snapshot;
import org.folio.rest.jaxrs.model.feesfines.Accountdata;
import org.folio.rest.jaxrs.model.feesfines.actions.Feefineactiondata;
import org.folio.rest.jaxrs.model.inventory.Holdingsrecord;
import org.folio.rest.jaxrs.model.inventory.Holdingsrecords;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import io.vertx.core.json.JsonObject;

@Service
public class OkapiService {

  private static final int SETTING_LIMIT = 1000;

  private static final String IDENTIFIER_TYPES_URL = "/identifier-types?limit=" + SETTING_LIMIT;
  private static final String CLASSIFICATION_TYPES_URL = "/classification-types?limit=" + SETTING_LIMIT;
  private static final String INSTANCE_TYPES_URL = "/instance-types?limit=" + SETTING_LIMIT;
  private static final String INSTANCE_FORMATS_URL = "/instance-formats?limit=" + SETTING_LIMIT;
  private static final String CONTRIBUTOR_TYPES_URL = "/contributor-types?limit=" + SETTING_LIMIT;
  private static final String CONTRIBUTOR_NAME_TYPES_URL = "/contributor-name-types?limit=" + SETTING_LIMIT;
  private static final String ELECTRONIC_ACCESS_URL = "/electronic-access-relationships?limit=" + SETTING_LIMIT;
  private static final String INSTANCE_NOTE_TYPES_URL = "/instance-note-types?limit=" + SETTING_LIMIT;
  private static final String INSTANCE_ALTERNATIVE_TITLE_TYPES_URL = "/alternative-title-types?limit=" + SETTING_LIMIT;
  private static final String ISSUANCE_MODES_URL = "/modes-of-issuance?limit=" + SETTING_LIMIT;
  private static final String INSTANCE_STATUSES_URL = "/instance-statuses?limit=" + SETTING_LIMIT;
  private static final String NATURE_OF_CONTENT_TERMS_URL = "/nature-of-content-terms?limit=" + SETTING_LIMIT;
  private static final String INSTANCE_RELATIONSHIP_TYPES_URL = "/instance-relationship-types?limit=" + SETTING_LIMIT;
  private static final String HOLDINGS_TYPES_URL = "/holdings-types?limit=" + SETTING_LIMIT;
  private static final String HOLDINGS_NOTE_TYPES_URL = "/holdings-note-types?limit=" + SETTING_LIMIT;
  private static final String ILL_POLICIES_URL = "/ill-policies?limit=" + SETTING_LIMIT;
  private static final String CALL_NUMBER_TYPES_URL = "/call-number-types?limit=" + SETTING_LIMIT;
  private static final String STATISTICAL_CODES_URL = "/statistical-codes?limit=" + SETTING_LIMIT;
  private static final String STATISTICAL_CODE_TYPES_URL = "/statistical-code-types?limit=" + SETTING_LIMIT;
  private static final String LOCATIONS_URL = "/locations?limit=" + SETTING_LIMIT;
  private static final String MATERIAL_TYPES_URL = "/material-types?limit=" + SETTING_LIMIT;
  private static final String ITEM_DAMAGED_STATUSES_URL = "/item-damaged-statuses?limit=" + SETTING_LIMIT;
  private static final String LOAN_TYPES_URL = "/loan-types?limit=" + SETTING_LIMIT;
  private static final String ITEM_NOTE_TYPES_URL = "/item-note-types?limit=" + SETTING_LIMIT;
  private static final String FIELD_PROTECTION_SETTINGS_URL = "/field-protection-settings/marc?limit=" + SETTING_LIMIT;

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

  public String loadReferenceData(ReferenceDatum referenceDatum) {
    switch (referenceDatum.getAction()) {
      case CREATE:
        String createUrl = okapi.getUrl() + referenceDatum.getPath();
        HttpEntity<JsonNode> createEntity = new HttpEntity<>(referenceDatum.getData(), headers(referenceDatum.getTenant(), referenceDatum.getToken()));
        ResponseEntity<String> createResponse = restTemplate.exchange(createUrl, HttpMethod.POST, createEntity, String.class);
        return createResponse.getBody();
      case UPDATE:
        String updateUrl = okapi.getUrl() + referenceDatum.getPath();
        if (referenceDatum.getData().has("id")) {
          String id = referenceDatum.getData().get("id").asText();
          updateUrl += "/" + id;
        }
        HttpEntity<JsonNode> updateEntity = new HttpEntity<>(referenceDatum.getData(), headers(referenceDatum.getTenant(), referenceDatum.getToken()));
        ResponseEntity<String> updateResponse = restTemplate.exchange(updateUrl, HttpMethod.PUT, updateEntity, String.class);
        return updateResponse.getBody();
      default:
        return "Unknown action";
    }
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

  public Snapshot createSnapshot(Snapshot snapshot, String tenant, String token) {
    String url = okapi.getUrl() + "/source-storage/snapshots";
    HttpEntity<Snapshot> entity = new HttpEntity<>(snapshot, headers(tenant, token));
    ResponseEntity<Snapshot> response = restTemplate.exchange(url, HttpMethod.POST, entity, Snapshot.class);
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

  public void updateHridSettings(JsonObject hridSettings, String tenant, String token) {
    HttpEntity<?> entity = new HttpEntity<>(hridSettings.getMap(), headers(tenant, token));
    String url = okapi.getUrl() + "/hrid-settings-storage/hrid-settings";
    restTemplate.exchange(url, HttpMethod.PUT, entity, Void.class);
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

  public Servicepoints fetchServicepoints(String tenant, String token) {
    HttpEntity<?> entity = new HttpEntity<>(headers(tenant, token));
    String url = okapi.getUrl() + "/service-points?limit=9999";
    ResponseEntity<Servicepoints> response = restTemplate.exchange(url, HttpMethod.GET, entity, Servicepoints.class);
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

  public JsonObject fetchRules(String tenant, String token) {
    HttpEntity<?> entity = new HttpEntity<>(headers(tenant, token));
    String url = okapi.getUrl() + "/mapping-rules";
    ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
    return new JsonObject(response.getBody());
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

  public Locations fetchLocations(String tenant, String token) {
    HttpEntity<?> entity = new HttpEntity<>(headers(tenant, token));
    String url = okapi.getUrl() + "/locations?limit=9999";
    ResponseEntity<Locations> response = restTemplate.exchange(url, HttpMethod.GET, entity, Locations.class);
    return response.getBody();
  }

  public Loantypes fetchLoantypes(String tenant, String token) {
    HttpEntity<?> entity = new HttpEntity<>(headers(tenant, token));
    String url = okapi.getUrl() + "/loan-types?limit=999";
    ResponseEntity<Loantypes> response = restTemplate.exchange(url, HttpMethod.GET, entity, Loantypes.class);
    return response.getBody();
  }

  public Holdingsrecords fetchHoldingsrecordsByIdAndInstanceId(String tenant, String token, String id, String instanceId) {
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

  public CompositePurchaseOrder postCompositePurchaseOrder(String tenant, String token, CompositePurchaseOrder compositePurchaseOrder) {
    HttpEntity<CompositePurchaseOrder> entity = new HttpEntity<>(compositePurchaseOrder, headers(tenant, token));
    String url = okapi.getUrl() + "/orders/composite-orders";
    ResponseEntity<CompositePurchaseOrder> response = restTemplate.exchange(url, HttpMethod.POST, entity, CompositePurchaseOrder.class);
    return response.getBody();
  }

  public MappingParameters getMappingParamaters(String tenant, String token) {
    HttpHeaders headers = headers(tenant, token);
    return new MappingParameters()
      .withInitializedState(true)
      .withIdentifierTypes(getIdentifierTypes(headers))
      .withClassificationTypes(getClassificationTypes(headers))
      .withInstanceTypes(getInstanceTypes(headers))
      .withElectronicAccessRelationships(getElectronicAccessRelationships(headers))
      .withInstanceFormats(getInstanceFormats(headers))
      .withContributorTypes(getContributorTypes(headers))
      .withContributorNameTypes(getContributorNameTypes(headers))
      .withInstanceNoteTypes(getInstanceNoteTypes(headers))
      .withAlternativeTitleTypes(getAlternativeTitleTypes(headers))
      .withIssuanceModes(getIssuanceModes(headers))
      .withInstanceStatuses(getInstanceStatuses(headers))
      .withNatureOfContentTerms(getNatureOfContentTerms(headers))
      .withInstanceRelationshipTypes(getInstanceRelationshipTypes(headers))
      .withInstanceRelationshipTypes(getInstanceRelationshipTypes(headers))
      .withHoldingsTypes(getHoldingsTypes(headers))
      .withHoldingsNoteTypes(getHoldingsNoteTypes(headers))
      .withIllPolicies(getIllPolicies(headers))
      .withCallNumberTypes(getCallNumberTypes(headers))
      .withStatisticalCodes(getStatisticalCodes(headers))
      .withStatisticalCodeTypes(getStatisticalCodeTypes(headers))
      .withLocations(getLocations(headers))
      .withMaterialTypes(getMaterialTypes(headers))
      .withItemDamagedStatuses(getItemDamagedStatuses(headers))
      .withLoanTypes(getLoanTypes(headers))
      .withItemNoteTypes(getItemNoteTypes(headers))
      .withMarcFieldProtectionSettings(getMarcFieldProtectionSettings(headers));
  }
  
  private List<org.folio.IdentifierType> getIdentifierTypes(HttpHeaders headers) {
    HttpEntity<org.folio.Identifiertypes> entity = new HttpEntity<>(headers);
    String url = okapi.getUrl() + IDENTIFIER_TYPES_URL;
    ResponseEntity<org.folio.Identifiertypes> response = restTemplate.exchange(url, HttpMethod.GET, entity, org.folio.Identifiertypes.class);
    return response.getBody().getIdentifierTypes();
  }

  private List<org.folio.ClassificationType> getClassificationTypes(HttpHeaders headers) {
    HttpEntity<org.folio.Classificationtypes> entity = new HttpEntity<>(headers);
    String url = okapi.getUrl() + CLASSIFICATION_TYPES_URL;
    ResponseEntity<org.folio.Classificationtypes> response = restTemplate.exchange(url, HttpMethod.GET, entity, org.folio.Classificationtypes.class);
    return response.getBody().getClassificationTypes();
  }

  private List<org.folio.InstanceType> getInstanceTypes(HttpHeaders headers) {
    HttpEntity<org.folio.Instancetypes> entity = new HttpEntity<>(headers);
    String url = okapi.getUrl() + INSTANCE_TYPES_URL;
    ResponseEntity<org.folio.Instancetypes> response = restTemplate.exchange(url, HttpMethod.GET, entity, org.folio.Instancetypes.class);
    return response.getBody().getInstanceTypes();
  }

  private List<org.folio.ElectronicAccessRelationship> getElectronicAccessRelationships(HttpHeaders headers) {
    HttpEntity<org.folio.Electronicaccessrelationships> entity = new HttpEntity<>(headers);
    String url = okapi.getUrl() + ELECTRONIC_ACCESS_URL;
    ResponseEntity<org.folio.Electronicaccessrelationships> response = restTemplate.exchange(url, HttpMethod.GET, entity, org.folio.Electronicaccessrelationships.class);
    return response.getBody().getElectronicAccessRelationships();
  }

  private List<org.folio.InstanceFormat> getInstanceFormats(HttpHeaders headers) {
    HttpEntity<org.folio.Instanceformats> entity = new HttpEntity<>(headers);
    String url = okapi.getUrl() + INSTANCE_FORMATS_URL;
    ResponseEntity<org.folio.Instanceformats> response = restTemplate.exchange(url, HttpMethod.GET, entity, org.folio.Instanceformats.class);
    return response.getBody().getInstanceFormats();
  }

  private List<org.folio.ContributorType> getContributorTypes(HttpHeaders headers) {
    HttpEntity<org.folio.Contributortypes> entity = new HttpEntity<>(headers);
    String url = okapi.getUrl() + CONTRIBUTOR_TYPES_URL;
    ResponseEntity<org.folio.Contributortypes> response = restTemplate.exchange(url, HttpMethod.GET, entity, org.folio.Contributortypes.class);
    return response.getBody().getContributorTypes();
  }

  private List<org.folio.ContributorNameType> getContributorNameTypes(HttpHeaders headers) {
    HttpEntity<org.folio.Contributornametypes> entity = new HttpEntity<>(headers);
    String url = okapi.getUrl() + CONTRIBUTOR_NAME_TYPES_URL;
    ResponseEntity<org.folio.Contributornametypes> response = restTemplate.exchange(url, HttpMethod.GET, entity, org.folio.Contributornametypes.class);
    return response.getBody().getContributorNameTypes();
  }

  private List<org.folio.InstanceNoteType> getInstanceNoteTypes(HttpHeaders headers) {
    HttpEntity<org.folio.Instancenotetypes> entity = new HttpEntity<>(headers);
    String url = okapi.getUrl() + INSTANCE_NOTE_TYPES_URL;
    ResponseEntity<org.folio.Instancenotetypes> response = restTemplate.exchange(url, HttpMethod.GET, entity, org.folio.Instancenotetypes.class);
    return response.getBody().getInstanceNoteTypes();
  }

  private List<org.folio.AlternativeTitleType> getAlternativeTitleTypes(HttpHeaders headers) {
    HttpEntity<org.folio.Alternativetitletypes> entity = new HttpEntity<>(headers);
    String url = okapi.getUrl() + INSTANCE_ALTERNATIVE_TITLE_TYPES_URL;
    ResponseEntity<org.folio.Alternativetitletypes> response = restTemplate.exchange(url, HttpMethod.GET, entity, org.folio.Alternativetitletypes.class);
    return response.getBody().getAlternativeTitleTypes();
  }

  private List<org.folio.NatureOfContentTerm> getNatureOfContentTerms(HttpHeaders headers) {
    HttpEntity<org.folio.Natureofcontentterms> entity = new HttpEntity<>(headers);
    String url = okapi.getUrl() + NATURE_OF_CONTENT_TERMS_URL;
    ResponseEntity<org.folio.Natureofcontentterms> response = restTemplate.exchange(url, HttpMethod.GET, entity, org.folio.Natureofcontentterms.class);
    return response.getBody().getNatureOfContentTerms();
  }

  private List<org.folio.InstanceStatus> getInstanceStatuses(HttpHeaders headers) {
    HttpEntity<org.folio.Instancestatuses> entity = new HttpEntity<>(headers);
    String url = okapi.getUrl() + INSTANCE_STATUSES_URL;
    ResponseEntity<org.folio.Instancestatuses> response = restTemplate.exchange(url, HttpMethod.GET, entity, org.folio.Instancestatuses.class);
    return response.getBody().getInstanceStatuses();
  }

  private List<org.folio.InstanceRelationshipType> getInstanceRelationshipTypes(HttpHeaders headers) {
    HttpEntity<org.folio.Instancerelationshiptypes> entity = new HttpEntity<>(headers);
    String url = okapi.getUrl() + INSTANCE_RELATIONSHIP_TYPES_URL;
    ResponseEntity<org.folio.Instancerelationshiptypes> response = restTemplate.exchange(url, HttpMethod.GET, entity, org.folio.Instancerelationshiptypes.class);
    return response.getBody().getInstanceRelationshipTypes();
  }

  private List<org.folio.HoldingsType> getHoldingsTypes(HttpHeaders headers) {
    HttpEntity<org.folio.Holdingstypes> entity = new HttpEntity<>(headers);
    String url = okapi.getUrl() + HOLDINGS_TYPES_URL;
    ResponseEntity<org.folio.Holdingstypes> response = restTemplate.exchange(url, HttpMethod.GET, entity, org.folio.Holdingstypes.class);
    return response.getBody().getHoldingsTypes();
  }

  private List<org.folio.HoldingsNoteType> getHoldingsNoteTypes(HttpHeaders headers) {
    HttpEntity<org.folio.Holdingsnotetypes> entity = new HttpEntity<>(headers);
    String url = okapi.getUrl() + HOLDINGS_NOTE_TYPES_URL;
    ResponseEntity<org.folio.Holdingsnotetypes> response = restTemplate.exchange(url, HttpMethod.GET, entity, org.folio.Holdingsnotetypes.class);
    return response.getBody().getHoldingsNoteTypes();
  }

  private List<org.folio.IllPolicy> getIllPolicies(HttpHeaders headers) {
    HttpEntity<org.folio.Illpolicies> entity = new HttpEntity<>(headers);
    String url = okapi.getUrl() + ILL_POLICIES_URL;
    ResponseEntity<org.folio.Illpolicies> response = restTemplate.exchange(url, HttpMethod.GET, entity, org.folio.Illpolicies.class);
    return response.getBody().getIllPolicies();
  }

  private List<org.folio.CallNumberType> getCallNumberTypes(HttpHeaders headers) {
    HttpEntity<org.folio.Callnumbertypes> entity = new HttpEntity<>(headers);
    String url = okapi.getUrl() + CALL_NUMBER_TYPES_URL;
    ResponseEntity<org.folio.Callnumbertypes> response = restTemplate.exchange(url, HttpMethod.GET, entity, org.folio.Callnumbertypes.class);
    return response.getBody().getCallNumberTypes();
  }

  private List<org.folio.StatisticalCode> getStatisticalCodes(HttpHeaders headers) {
    HttpEntity<org.folio.Statisticalcodes> entity = new HttpEntity<>(headers);
    String url = okapi.getUrl() + STATISTICAL_CODES_URL;
    ResponseEntity<org.folio.Statisticalcodes> response = restTemplate.exchange(url, HttpMethod.GET, entity, org.folio.Statisticalcodes.class);
    return response.getBody().getStatisticalCodes();
  }

  private List<org.folio.StatisticalCodeType> getStatisticalCodeTypes(HttpHeaders headers) {
    HttpEntity<org.folio.Statisticalcodetypes> entity = new HttpEntity<>(headers);
    String url = okapi.getUrl() + STATISTICAL_CODE_TYPES_URL;
    ResponseEntity<org.folio.Statisticalcodetypes> response = restTemplate.exchange(url, HttpMethod.GET, entity, org.folio.Statisticalcodetypes.class);
    return response.getBody().getStatisticalCodeTypes();
  }

  private List<org.folio.Location> getLocations(HttpHeaders headers) {
    HttpEntity<org.folio.Locations> entity = new HttpEntity<>(headers);
    String url = okapi.getUrl() + LOCATIONS_URL;
    ResponseEntity<org.folio.Locations> response = restTemplate.exchange(url, HttpMethod.GET, entity, org.folio.Locations.class);
    return response.getBody().getLocations();
  }

  private List<org.folio.Mtype> getMaterialTypes(HttpHeaders headers) {
    HttpEntity<org.folio.Materialtypes> entity = new HttpEntity<>(headers);
    String url = okapi.getUrl() + MATERIAL_TYPES_URL;
    ResponseEntity<org.folio.Materialtypes> response = restTemplate.exchange(url, HttpMethod.GET, entity, org.folio.Materialtypes.class);
    return response.getBody().getMtypes();
  }

  private List<org.folio.ItemDamageStatus> getItemDamagedStatuses(HttpHeaders headers) {
    HttpEntity<org.folio.Itemdamagedstatuses> entity = new HttpEntity<>(headers);
    String url = okapi.getUrl() + ITEM_DAMAGED_STATUSES_URL;
    ResponseEntity<org.folio.Itemdamagedstatuses> response = restTemplate.exchange(url, HttpMethod.GET, entity, org.folio.Itemdamagedstatuses.class);
    return response.getBody().getItemDamageStatuses();
  }

  private List<org.folio.Loantype> getLoanTypes(HttpHeaders headers) {
    HttpEntity<org.folio.Loantypes> entity = new HttpEntity<>(headers);
    String url = okapi.getUrl() + LOAN_TYPES_URL;
    ResponseEntity<org.folio.Loantypes> response = restTemplate.exchange(url, HttpMethod.GET, entity, org.folio.Loantypes.class);
    return response.getBody().getLoantypes();
  }

  private List<org.folio.ItemNoteType> getItemNoteTypes(HttpHeaders headers) {
    HttpEntity<org.folio.Itemnotetypes> entity = new HttpEntity<>(headers);
    String url = okapi.getUrl() + ITEM_NOTE_TYPES_URL;
    ResponseEntity<org.folio.Itemnotetypes> response = restTemplate.exchange(url, HttpMethod.GET, entity, org.folio.Itemnotetypes.class);
    return response.getBody().getItemNoteTypes();
  }

  private List<org.folio.rest.jaxrs.model.MarcFieldProtectionSetting> getMarcFieldProtectionSettings(HttpHeaders headers) {
    HttpEntity<org.folio.MarcFieldProtectionSettingsCollection> entity = new HttpEntity<>(headers);
    String url = okapi.getUrl() + FIELD_PROTECTION_SETTINGS_URL;
    ResponseEntity<org.folio.MarcFieldProtectionSettingsCollection> response = restTemplate.exchange(url, HttpMethod.GET, entity, org.folio.MarcFieldProtectionSettingsCollection.class);
    return response.getBody().getMarcFieldProtectionSettings();
  }

  private List<org.folio.IssuanceMode> getIssuanceModes(HttpHeaders headers) {
    HttpEntity<org.folio.Issuancemodes> entity = new HttpEntity<>(headers);
    String url = okapi.getUrl() + ISSUANCE_MODES_URL;
    ResponseEntity<org.folio.Issuancemodes> response = restTemplate.exchange(url, HttpMethod.GET, entity, org.folio.Issuancemodes.class);
    return response.getBody().getIssuanceModes();
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
