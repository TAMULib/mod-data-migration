package org.folio.rest.migration.model;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.codehaus.plexus.util.StringUtils;
import org.folio.rest.jaxrs.model.HoldingsStatement;
import org.folio.rest.jaxrs.model.HoldingsStatementsForIndex;
import org.folio.rest.jaxrs.model.HoldingsStatementsForSupplement;
import org.folio.rest.jaxrs.model.Holdingsrecord;
import org.folio.rest.jaxrs.model.Note;
import org.folio.rest.migration.mapping.HoldingMapper;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.marc.VariableField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonObject;

public class HoldingRecord {

  private final static Logger log = LoggerFactory.getLogger(HoldingRecord.class);

  private final Record record;

  private final String mfhdId;
  private final String locationId;

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

  public HoldingRecord(Record record, String mfhdId, String locationId, Boolean discoverySuppress, String callNumber, String callNumberType,
      String holdingsType, String receiptStatus, String acquisitionMethod, String retentionPolicy) {
    this.record = record;
    this.mfhdId = mfhdId;
    this.locationId = locationId;
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

  public Holdingsrecord toHolding(HoldingMapper holdingMapper, String hridPrefix, int hrid) throws JsonProcessingException {
    final Holdingsrecord holding = holdingMapper.getHolding(parsedRecord);
    if (Objects.nonNull(holding)) {
      holding.setId(holdingId);
      holding.setInstanceId(instanceId);
      holding.setPermanentLocationId(locationId);
      // holding.setTemporaryLocationId(null);
      holding.setHoldingsTypeId(holdingsType);
      holding.setCallNumberTypeId(callNumberType);
      // holding.setIllPolicyId(null);

      holding.setDiscoverySuppress(discoverySuppress);
      holding.setCallNumber(callNumber);
      holding.setReceiptStatus(receiptStatus);
      holding.setAcquisitionMethod(acquisitionMethod);
      holding.setRetentionPolicy(retentionPolicy);
      // holding.setStatisticalCodeIds(null);

      processMarcHolding(holding);

      holding.setHrid(String.format("%s%011d", hridPrefix, hrid));

      Set<String> formerIds = new HashSet<>();
      formerIds.add(mfhdId);
      holding.setFormerIds(formerIds);

      org.folio.rest.jaxrs.model.Metadata metadata = new org.folio.rest.jaxrs.model.Metadata();
      metadata.setCreatedByUserId(createdByUserId);
      metadata.setCreatedDate(createdDate);
      holding.setMetadata(metadata);
    }
    return holding;
  }

  public void processMarcHolding(Holdingsrecord holding) {
    process852Field(holding);
    process5xxFields(holding);
    process866Field(holding);
    process867Field(holding);
    process868Field(holding);
  }

  public void process852Field(Holdingsrecord holding) {
    List<Note> notes = holding.getNotes();
    StringBuilder callNumberBuilder = new StringBuilder(" ");
    DataField f852 = (DataField) record.getVariableField("852");
    if (Objects.nonNull(f852)) {
      f852.getSubfields().forEach(subfield -> {
        char code = subfield.getCode();
        String data = subfield.getData();
        switch (code) {
        case 'g': {
          Note note = new Note();
          note.setStaffOnly(false);
          note.setNote(data);
          note.setHoldingsNoteTypeId("7ca7dc63-c053-4aec-8272-c03aeda4840c");
          notes.add(note);
        } break;
        case 'h':
        case 'i':
          callNumberBuilder.append(data);
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
          note.setHoldingsNoteTypeId("b160f13a-ddba-4053-b9c4-60ec5ea45d56");
          notes.add(note);
        } break;
        case 'z': {
          // note
          Note note = new Note();
          note.setStaffOnly(false);
          note.setNote(data);
          note.setHoldingsNoteTypeId("b160f13a-ddba-4053-b9c4-60ec5ea45d56");
          notes.add(note);
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

  public void process5xxFields(Holdingsrecord holding) {
    List<Note> notes = holding.getNotes();
    List<VariableField> f5xx = record.getVariableFields(new String[] { "506", "541", "562", "583" });
    f5xx.stream().map(field -> (DataField) field).forEach(field -> {
      String tag = field.getTag();
      Note note = new Note();
      note.setNote(field.toString());
      switch (tag) {
      case "506":
        // access
        note.setStaffOnly(false);
        note.setHoldingsNoteTypeId("f453de0f-8b54-4e99-9180-52932529e3a6");
        break;
      case "541":
        // provenance
        note.setStaffOnly(false);
        note.setHoldingsNoteTypeId("db9b4787-95f0-4e78-becf-26748ce6bdeb");
        break;
      case "562":
        // copy
        note.setStaffOnly(false);
        note.setHoldingsNoteTypeId("c4407cc7-d79f-4609-95bd-1cefb2e2b5c5");
        break;
      case "583":
        // action
        note.setStaffOnly(true);
        note.setHoldingsNoteTypeId("d6510242-5ec3-42ed-b593-3585d2e48fd6");
        break;
      default:
        // do nothing
      }
      notes.add(note);
    });
  }

  public void process866Field(Holdingsrecord holding) {
    List<HoldingsStatement> holdingsStatements = holding.getHoldingsStatements();
    DataField f866 = (DataField) record.getVariableField("866");
    if (Objects.nonNull(f866)) {
      f866.getSubfields().forEach(subfield -> {
        char code = subfield.getCode();
        String data = subfield.getData();
        HoldingsStatement statement = new HoldingsStatement();
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
        holdingsStatements.add(statement);
      });
    }
  }

  public void process867Field(Holdingsrecord holding) {
    List<HoldingsStatementsForSupplement> holdingsStatements = holding.getHoldingsStatementsForSupplements();
    DataField f867 = (DataField) record.getVariableField("867");
    if (Objects.nonNull(f867)) {
      f867.getSubfields().forEach(subfield -> {
        char code = subfield.getCode();
        String data = subfield.getData();
        HoldingsStatementsForSupplement statement = new HoldingsStatementsForSupplement();
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
        holdingsStatements.add(statement);
      });
    }
  }

  public void process868Field(Holdingsrecord holding) {
    List<HoldingsStatementsForIndex> holdingsStatements = holding.getHoldingsStatementsForIndexes();
    DataField f868 = (DataField) record.getVariableField("868");
    if (Objects.nonNull(f868)) {
      f868.getSubfields().forEach(subfield -> {
        char code = subfield.getCode();
        String data = subfield.getData();
        HoldingsStatementsForIndex statement = new HoldingsStatementsForIndex();
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
        holdingsStatements.add(statement);
      });
    }
  }

}