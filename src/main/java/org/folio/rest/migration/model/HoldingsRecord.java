package org.folio.rest.migration.model;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.apache.commons.lang3.StringUtils;
import org.folio.rest.jaxrs.model.inventory.HoldingsStatement;
import org.folio.rest.jaxrs.model.inventory.HoldingsStatementsForIndex;
import org.folio.rest.jaxrs.model.inventory.HoldingsStatementsForSupplement;
import org.folio.rest.jaxrs.model.inventory.Holdingsrecord;
import org.folio.rest.jaxrs.model.inventory.Metadata;
import org.folio.rest.jaxrs.model.inventory.Note;
import org.folio.rest.migration.mapping.HoldingMapper;
import org.folio.rest.migration.model.request.holdings.HoldingsMaps;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonObject;

public class HoldingsRecord {

  private final static Logger log = LoggerFactory.getLogger(HoldingsRecord.class);

  private final HoldingsMaps holdingMaps;

  private final Record record;

  private final String mfhdId;
  private final String locationId;
  private final Set<String> statisticalCodes;

  private final Boolean discoverySuppress;

  private final String callNumber;
  private final String callNumberType;
  private final String holdingsType;
  private final String receiptStatus;
  private final String acquisitionMethod;
  private final String retentionPolicy;

  private JsonObject parsedRecord;

  private String holdingId;
  private String instanceId;

  private String createdByUserId;
  private Date createdDate;

  public HoldingsRecord(HoldingsMaps holdingMaps, Record record, String mfhdId, String locationId, Set<String> statisticalCodes, Boolean discoverySuppress,
    String callNumber, String callNumberType, String holdingsType, String receiptStatus, String acquisitionMethod, String retentionPolicy) {
    this.holdingMaps = holdingMaps;
    this.record = record;
    this.mfhdId = mfhdId;
    this.locationId = locationId;
    this.statisticalCodes = statisticalCodes;
    this.discoverySuppress = discoverySuppress;
    this.callNumber = callNumber;
    this.callNumberType = callNumberType;
    this.holdingsType = holdingsType;
    this.receiptStatus = receiptStatus;
    this.acquisitionMethod = acquisitionMethod;
    this.retentionPolicy = retentionPolicy;
  }

  public String getMfhdId() {
    return mfhdId;
  }

  public String getLocationId() {
    return locationId;
  }

  public Boolean getDiscoverySuppress() {
    return discoverySuppress;
  }

  public String getCallNumber() {
    return callNumber;
  }

  public String getCallNumberType() {
    return callNumberType;
  }

  public String getHoldingsType() {
    return holdingsType;
  }

  public String getReceiptStatus() {
    return receiptStatus;
  }

  public String getAcquisitionMethod() {
    return acquisitionMethod;
  }

  public String getRetentionPolicy() {
    return retentionPolicy;
  }

  public Record getRecord() {
    return this.record;
  }

  public JsonObject getParsedRecord() {
    return parsedRecord;
  }

  public void setParsedRecord(JsonObject parsedRecord) {
    this.parsedRecord = parsedRecord;
  }

  public String getHoldingId() {
    return holdingId;
  }

  public void setHoldingId(String holdingId) {
    this.holdingId = holdingId;
  }

  public String getInstanceId() {
    return instanceId;
  }

  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  public String getCreatedByUserId() {
    return createdByUserId;
  }

  public void setCreatedByUserId(String createdByUserId) {
    this.createdByUserId = createdByUserId;
  }

  public Date getCreatedDate() {
    return createdDate;
  }

  public void setCreatedDate(Date createdDate) {
    this.createdDate = createdDate;
  }

  public Holdingsrecord toHolding(HoldingMapper holdingMapper, HoldingsMaps holdingsMaps, String hridString) throws JsonProcessingException {
    final Holdingsrecord holdings = holdingMapper.getHolding(parsedRecord);
    if (Objects.nonNull(holdings)) {
      holdings.setId(holdingId);
      holdings.setInstanceId(instanceId);
      holdings.setPermanentLocationId(locationId);
      // holding.setTemporaryLocationId(null);
      holdings.setHoldingsTypeId(holdingsType);

      if (Objects.nonNull(callNumberType)) {
        holdings.setCallNumberTypeId(callNumberType);
      }

      // holding.setIllPolicyId(null);

      holdings.setDiscoverySuppress(discoverySuppress);
      holdings.setCallNumber(callNumber);
      holdings.setReceiptStatus(receiptStatus);
      holdings.setAcquisitionMethod(acquisitionMethod);
      holdings.setRetentionPolicy(retentionPolicy);
      holdings.setStatisticalCodeIds(statisticalCodes);

      processMarcHolding(holdings, holdingsMaps.getFieldRegexExclusion());

      holdings.setHrid(hridString);

      Set<String> formerIds = new HashSet<>();
      formerIds.add(mfhdId);
      holdings.setFormerIds(formerIds);

      Metadata metadata = new Metadata();
      metadata.setCreatedByUserId(createdByUserId);
      metadata.setCreatedDate(createdDate);
      metadata.setUpdatedByUserId(createdByUserId);
      metadata.setUpdatedDate(createdDate);
      holdings.setMetadata(metadata);
    }
    return holdings;
  }

  public void processMarcHolding(Holdingsrecord holdings, Map<String, String> fieldRegexExclusion) {
    process5xxFields(holdings, fieldRegexExclusion);
    process852Field(holdings);
    process866Field(holdings);
    process867Field(holdings);
    process868Field(holdings);
  }

  public void process5xxFields(Holdingsrecord holdings, Map<String, String> fieldRegexExclusion) {
    record.getVariableFields().stream()
      .filter(Objects::nonNull)
      .filter(field -> field instanceof DataField)
      .map(field -> (DataField) field)
      .filter(field -> StringUtils.isNotEmpty(field.getTag()))
      .filter(field -> field.getTag().startsWith("5"))
      .forEach(field -> {
        String tag = field.getTag();
        Optional<String> regexExclusion = Optional.ofNullable(fieldRegexExclusion.get(tag));
        if (regexExclusion.isPresent() && field.toString().matches(regexExclusion.get())) {
          log.debug("Skipping field {} by exclusion regex {}", field.toString(), regexExclusion.get());
        } else {
          Note note = new Note();
          note.setNote(field.getSubfields().stream().map(Subfield::getData).collect(Collectors.joining(". ")));
          switch (tag) {
          case "506":
            // access
            note.setStaffOnly(false);
            note.setHoldingsNoteTypeId(holdingMaps.getHoldingsNotesType().get("access"));
            break;
          case "541":
            // source_of_acq
            note.setStaffOnly(field.getIndicator1() != '1');
            note.setHoldingsNoteTypeId(holdingMaps.getHoldingsNotesType().get("source_of_acq"));
            break;
          case "561":
            // provenance
            note.setStaffOnly(field.getIndicator1() != '1');
            note.setHoldingsNoteTypeId(holdingMaps.getHoldingsNotesType().get("provenance"));
            break;
          case "562":
            // copy
            note.setStaffOnly(false);
            note.setHoldingsNoteTypeId(holdingMaps.getHoldingsNotesType().get("copy"));
            break;
          case "563":
            // binding
            note.setStaffOnly(false);
            note.setHoldingsNoteTypeId(holdingMaps.getHoldingsNotesType().get("binding"));
            break;
          case "583":
            Subfield subfield = field.getSubfield('z');
            if (Objects.nonNull(subfield)) {
              Note zNote = new Note();
              zNote.setStaffOnly(false);
              zNote.setNote(subfield.getData());
              zNote.setHoldingsNoteTypeId(holdingMaps.getHoldingsNotesType().get("note"));
              holdings.getNotes().add(zNote);

              note.setNote(field.getSubfields().stream().filter(sf -> sf.getCode() != 'z').map(Subfield::getData).collect(Collectors.joining(". ")));
            }
            // action
            note.setStaffOnly(true);
            note.setHoldingsNoteTypeId(holdingMaps.getHoldingsNotesType().get("action"));
            break;
          default:
            note.setStaffOnly(true);
            note.setHoldingsNoteTypeId(holdingMaps.getHoldingsNotesType().get("note"));
            break;
          }
          holdings.getNotes().add(note);
        }
      });
  }

  public void process852Field(Holdingsrecord holding) {
    StringBuilder callNumberBuilder = new StringBuilder();
    DataField f852 = (DataField) record.getVariableField("852");
    if (Objects.nonNull(f852)) {
      f852.getSubfields().forEach(subfield -> {
        char code = subfield.getCode();
        String data = subfield.getData();
        switch (code) {
        case 'g': {
          // latest_in
          Note note = new Note();
          note.setStaffOnly(false);
          note.setNote(data);
          note.setHoldingsNoteTypeId(holdingMaps.getHoldingsNotesType().get("latest_in"));
          holding.getNotes().add(note);
        } break;
        case 'h':
        case 'i':
          callNumberBuilder
            .append(StringUtils.SPACE)
            .append(data);
          break;
        case 'k':
          holding.setCallNumberPrefix(data);
          break;
        case 'l':
          holding.setShelvingTitle(data);
          break;
        case 'm':
          holding.setCallNumberSuffix(data);
          break;
        case 't':
          // do nothing
          break;
        case 'x': {
          // note
          Note note = new Note();
          note.setStaffOnly(true);
          note.setNote(data);
          note.setHoldingsNoteTypeId(holdingMaps.getHoldingsNotesType().get("note"));
          holding.getNotes().add(note);
        } break;
        case 'z': {
          // note
          Note note = new Note();
          note.setStaffOnly(false);
          note.setNote(data);
          note.setHoldingsNoteTypeId(holdingMaps.getHoldingsNotesType().get("note"));
          holding.getNotes().add(note);
        } break;
        case 'b':
          // do nothing
          break;
        default:
          log.debug("Unknown 852 subfield {} in {}", subfield, mfhdId);
        }
      });
      String callNumber = callNumberBuilder.toString().trim();
      if (StringUtils.isNotEmpty(callNumber)) {
        holding.setCallNumber(callNumber);
      }
    }
  }

  public void process866Field(Holdingsrecord holding) {
    List<HoldingsStatement> holdingsStatements = holding.getHoldingsStatements();
    record.getVariableFields("866").stream().map(vf -> (DataField) vf).forEach(f866 -> {
      HoldingsStatement statement = new HoldingsStatement();
      f866.getSubfields().forEach(subfield -> {
        char code = subfield.getCode();
        String data = subfield.getData();
        switch (code) {
        case 'a':
          statement.setStatement(data);
          break;
        case 'x':
        case 'z':
          // NOTE: this may require note id from a prefabbed note
          statement.setNote(data);
          break;
        default:
          // do nothing
        }
      });
      holdingsStatements.add(statement);
    });
  }

  public void process867Field(Holdingsrecord holding) {
    List<HoldingsStatementsForSupplement> holdingsStatements = holding.getHoldingsStatementsForSupplements();
    record.getVariableFields("867").stream().map(vf -> (DataField) vf).forEach(f867 -> {
      HoldingsStatementsForSupplement statement = new HoldingsStatementsForSupplement();
      f867.getSubfields().forEach(subfield -> {
        char code = subfield.getCode();
        String data = subfield.getData();
        switch (code) {
        case 'a':
          statement.setStatement(data);
          break;
        case 'x':
        case 'z':
          // NOTE: this may require note id from a prefabbed note
          statement.setNote(data);
          break;
        default:
          // do nothing
        }
      });
      holdingsStatements.add(statement);
    });
  }

  public void process868Field(Holdingsrecord holding) {
    List<HoldingsStatementsForIndex> holdingsStatements = holding.getHoldingsStatementsForIndexes();
    record.getVariableFields("868").stream().map(vf -> (DataField) vf).forEach(f868 -> {
      HoldingsStatementsForIndex statement = new HoldingsStatementsForIndex();
      f868.getSubfields().forEach(subfield -> {
        char code = subfield.getCode();
        String data = subfield.getData();
        switch (code) {
        case 'a':
          statement.setStatement(data);
          break;
        case 'x':
        case 'z':
          // NOTE: this may require note id from a prefabbed note
          statement.setNote(data);
          break;
        default:
          // do nothing
        }
      });
      holdingsStatements.add(statement);
    });
  }

}
