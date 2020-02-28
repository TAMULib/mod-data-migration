
package org.folio.rest.migration.model.generated.source_record_storage;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;
import org.folio.rest.migration.model.generated.common.ExternalIdsHolder;
import org.folio.rest.migration.model.generated.inventory_storage.Metadata;


/**
 * Record Data Model
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "snapshotId",
    "matchedProfileId",
    "matchedId",
    "generation",
    "recordType",
    "rawRecordId",
    "parsedRecordId",
    "errorRecordId",
    "externalIdsHolder",
    "deleted",
    "order",
    "additionalInfo",
    "metadata"
})
public class RecordModel {

    /**
     * Regexp pattern for UUID validation
     * 
     */
    @JsonProperty("id")
    @JsonPropertyDescription("Regexp pattern for UUID validation")
    private String id;
    /**
     * Regexp pattern for UUID validation
     * (Required)
     * 
     */
    @JsonProperty("snapshotId")
    @JsonPropertyDescription("Regexp pattern for UUID validation")
    private String snapshotId;
    /**
     * Regexp pattern for UUID validation
     * 
     */
    @JsonProperty("matchedProfileId")
    @JsonPropertyDescription("Regexp pattern for UUID validation")
    private String matchedProfileId;
    /**
     * Regexp pattern for UUID validation
     * (Required)
     * 
     */
    @JsonProperty("matchedId")
    @JsonPropertyDescription("Regexp pattern for UUID validation")
    private String matchedId;
    /**
     * Generation from the last record with the same matchedId incremented by 1. Starts from 0.
     * 
     */
    @JsonProperty("generation")
    @JsonPropertyDescription("Generation from the last record with the same matchedId incremented by 1. Starts from 0.")
    private Integer generation = 0;
    /**
     * Record Type Enum
     * (Required)
     * 
     */
    @JsonProperty("recordType")
    @JsonPropertyDescription("Record Type Enum")
    private RecordModel.RecordType recordType;
    /**
     * Regexp pattern for UUID validation
     * (Required)
     * 
     */
    @JsonProperty("rawRecordId")
    @JsonPropertyDescription("Regexp pattern for UUID validation")
    private String rawRecordId;
    /**
     * Regexp pattern for UUID validation
     * 
     */
    @JsonProperty("parsedRecordId")
    @JsonPropertyDescription("Regexp pattern for UUID validation")
    private String parsedRecordId;
    /**
     * Regexp pattern for UUID validation
     * 
     */
    @JsonProperty("errorRecordId")
    @JsonPropertyDescription("Regexp pattern for UUID validation")
    private String errorRecordId;
    /**
     * Contains identifiers of external entities (instance, holding)
     * 
     */
    @JsonProperty("externalIdsHolder")
    @JsonPropertyDescription("Contains identifiers of external entities (instance, holding)")
    private ExternalIdsHolder externalIdsHolder;
    /**
     * Flag indicates that the record marked as deleted
     * 
     */
    @JsonProperty("deleted")
    @JsonPropertyDescription("Flag indicates that the record marked as deleted")
    private Boolean deleted = false;
    /**
     * Order of the record in imported file
     * 
     */
    @JsonProperty("order")
    @JsonPropertyDescription("Order of the record in imported file")
    private Integer order;
    /**
     * Auxiliary data which is not related to MARC type record
     * 
     */
    @JsonProperty("additionalInfo")
    @JsonPropertyDescription("Auxiliary data which is not related to MARC type record")
    private AdditionalInfo additionalInfo;
    /**
     * Metadata Schema
     * <p>
     * Metadata about creation and changes to records, provided by the server (client should not provide)
     * 
     */
    @JsonProperty("metadata")
    @JsonPropertyDescription("Metadata about creation and changes to records, provided by the server (client should not provide)")
    private Metadata metadata;

    /**
     * Regexp pattern for UUID validation
     * 
     */
    @JsonProperty("id")
    public String getId() {
        return id;
    }

    /**
     * Regexp pattern for UUID validation
     * 
     */
    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Regexp pattern for UUID validation
     * (Required)
     * 
     */
    @JsonProperty("snapshotId")
    public String getSnapshotId() {
        return snapshotId;
    }

    /**
     * Regexp pattern for UUID validation
     * (Required)
     * 
     */
    @JsonProperty("snapshotId")
    public void setSnapshotId(String snapshotId) {
        this.snapshotId = snapshotId;
    }

    /**
     * Regexp pattern for UUID validation
     * 
     */
    @JsonProperty("matchedProfileId")
    public String getMatchedProfileId() {
        return matchedProfileId;
    }

    /**
     * Regexp pattern for UUID validation
     * 
     */
    @JsonProperty("matchedProfileId")
    public void setMatchedProfileId(String matchedProfileId) {
        this.matchedProfileId = matchedProfileId;
    }

    /**
     * Regexp pattern for UUID validation
     * (Required)
     * 
     */
    @JsonProperty("matchedId")
    public String getMatchedId() {
        return matchedId;
    }

    /**
     * Regexp pattern for UUID validation
     * (Required)
     * 
     */
    @JsonProperty("matchedId")
    public void setMatchedId(String matchedId) {
        this.matchedId = matchedId;
    }

    /**
     * Generation from the last record with the same matchedId incremented by 1. Starts from 0.
     * 
     */
    @JsonProperty("generation")
    public Integer getGeneration() {
        return generation;
    }

    /**
     * Generation from the last record with the same matchedId incremented by 1. Starts from 0.
     * 
     */
    @JsonProperty("generation")
    public void setGeneration(Integer generation) {
        this.generation = generation;
    }

    /**
     * Record Type Enum
     * (Required)
     * 
     */
    @JsonProperty("recordType")
    public RecordModel.RecordType getRecordType() {
        return recordType;
    }

    /**
     * Record Type Enum
     * (Required)
     * 
     */
    @JsonProperty("recordType")
    public void setRecordType(RecordModel.RecordType recordType) {
        this.recordType = recordType;
    }

    /**
     * Regexp pattern for UUID validation
     * (Required)
     * 
     */
    @JsonProperty("rawRecordId")
    public String getRawRecordId() {
        return rawRecordId;
    }

    /**
     * Regexp pattern for UUID validation
     * (Required)
     * 
     */
    @JsonProperty("rawRecordId")
    public void setRawRecordId(String rawRecordId) {
        this.rawRecordId = rawRecordId;
    }

    /**
     * Regexp pattern for UUID validation
     * 
     */
    @JsonProperty("parsedRecordId")
    public String getParsedRecordId() {
        return parsedRecordId;
    }

    /**
     * Regexp pattern for UUID validation
     * 
     */
    @JsonProperty("parsedRecordId")
    public void setParsedRecordId(String parsedRecordId) {
        this.parsedRecordId = parsedRecordId;
    }

    /**
     * Regexp pattern for UUID validation
     * 
     */
    @JsonProperty("errorRecordId")
    public String getErrorRecordId() {
        return errorRecordId;
    }

    /**
     * Regexp pattern for UUID validation
     * 
     */
    @JsonProperty("errorRecordId")
    public void setErrorRecordId(String errorRecordId) {
        this.errorRecordId = errorRecordId;
    }

    /**
     * Contains identifiers of external entities (instance, holding)
     * 
     */
    @JsonProperty("externalIdsHolder")
    public ExternalIdsHolder getExternalIdsHolder() {
        return externalIdsHolder;
    }

    /**
     * Contains identifiers of external entities (instance, holding)
     * 
     */
    @JsonProperty("externalIdsHolder")
    public void setExternalIdsHolder(ExternalIdsHolder externalIdsHolder) {
        this.externalIdsHolder = externalIdsHolder;
    }

    /**
     * Flag indicates that the record marked as deleted
     * 
     */
    @JsonProperty("deleted")
    public Boolean getDeleted() {
        return deleted;
    }

    /**
     * Flag indicates that the record marked as deleted
     * 
     */
    @JsonProperty("deleted")
    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    /**
     * Order of the record in imported file
     * 
     */
    @JsonProperty("order")
    public Integer getOrder() {
        return order;
    }

    /**
     * Order of the record in imported file
     * 
     */
    @JsonProperty("order")
    public void setOrder(Integer order) {
        this.order = order;
    }

    /**
     * Auxiliary data which is not related to MARC type record
     * 
     */
    @JsonProperty("additionalInfo")
    public AdditionalInfo getAdditionalInfo() {
        return additionalInfo;
    }

    /**
     * Auxiliary data which is not related to MARC type record
     * 
     */
    @JsonProperty("additionalInfo")
    public void setAdditionalInfo(AdditionalInfo additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    /**
     * Metadata Schema
     * <p>
     * Metadata about creation and changes to records, provided by the server (client should not provide)
     * 
     */
    @JsonProperty("metadata")
    public Metadata getMetadata() {
        return metadata;
    }

    /**
     * Metadata Schema
     * <p>
     * Metadata about creation and changes to records, provided by the server (client should not provide)
     * 
     */
    @JsonProperty("metadata")
    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(RecordModel.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("id");
        sb.append('=');
        sb.append(((this.id == null)?"<null>":this.id));
        sb.append(',');
        sb.append("snapshotId");
        sb.append('=');
        sb.append(((this.snapshotId == null)?"<null>":this.snapshotId));
        sb.append(',');
        sb.append("matchedProfileId");
        sb.append('=');
        sb.append(((this.matchedProfileId == null)?"<null>":this.matchedProfileId));
        sb.append(',');
        sb.append("matchedId");
        sb.append('=');
        sb.append(((this.matchedId == null)?"<null>":this.matchedId));
        sb.append(',');
        sb.append("generation");
        sb.append('=');
        sb.append(((this.generation == null)?"<null>":this.generation));
        sb.append(',');
        sb.append("recordType");
        sb.append('=');
        sb.append(((this.recordType == null)?"<null>":this.recordType));
        sb.append(',');
        sb.append("rawRecordId");
        sb.append('=');
        sb.append(((this.rawRecordId == null)?"<null>":this.rawRecordId));
        sb.append(',');
        sb.append("parsedRecordId");
        sb.append('=');
        sb.append(((this.parsedRecordId == null)?"<null>":this.parsedRecordId));
        sb.append(',');
        sb.append("errorRecordId");
        sb.append('=');
        sb.append(((this.errorRecordId == null)?"<null>":this.errorRecordId));
        sb.append(',');
        sb.append("externalIdsHolder");
        sb.append('=');
        sb.append(((this.externalIdsHolder == null)?"<null>":this.externalIdsHolder));
        sb.append(',');
        sb.append("deleted");
        sb.append('=');
        sb.append(((this.deleted == null)?"<null>":this.deleted));
        sb.append(',');
        sb.append("order");
        sb.append('=');
        sb.append(((this.order == null)?"<null>":this.order));
        sb.append(',');
        sb.append("additionalInfo");
        sb.append('=');
        sb.append(((this.additionalInfo == null)?"<null>":this.additionalInfo));
        sb.append(',');
        sb.append("metadata");
        sb.append('=');
        sb.append(((this.metadata == null)?"<null>":this.metadata));
        sb.append(',');
        if (sb.charAt((sb.length()- 1)) == ',') {
            sb.setCharAt((sb.length()- 1), ']');
        } else {
            sb.append(']');
        }
        return sb.toString();
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = ((result* 31)+((this.generation == null)? 0 :this.generation.hashCode()));
        result = ((result* 31)+((this.metadata == null)? 0 :this.metadata.hashCode()));
        result = ((result* 31)+((this.snapshotId == null)? 0 :this.snapshotId.hashCode()));
        result = ((result* 31)+((this.matchedProfileId == null)? 0 :this.matchedProfileId.hashCode()));
        result = ((result* 31)+((this.recordType == null)? 0 :this.recordType.hashCode()));
        result = ((result* 31)+((this.rawRecordId == null)? 0 :this.rawRecordId.hashCode()));
        result = ((result* 31)+((this.parsedRecordId == null)? 0 :this.parsedRecordId.hashCode()));
        result = ((result* 31)+((this.externalIdsHolder == null)? 0 :this.externalIdsHolder.hashCode()));
        result = ((result* 31)+((this.deleted == null)? 0 :this.deleted.hashCode()));
        result = ((result* 31)+((this.additionalInfo == null)? 0 :this.additionalInfo.hashCode()));
        result = ((result* 31)+((this.errorRecordId == null)? 0 :this.errorRecordId.hashCode()));
        result = ((result* 31)+((this.id == null)? 0 :this.id.hashCode()));
        result = ((result* 31)+((this.matchedId == null)? 0 :this.matchedId.hashCode()));
        result = ((result* 31)+((this.order == null)? 0 :this.order.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof RecordModel) == false) {
            return false;
        }
        RecordModel rhs = ((RecordModel) other);
        return (((((((((((((((this.generation == rhs.generation)||((this.generation!= null)&&this.generation.equals(rhs.generation)))&&((this.metadata == rhs.metadata)||((this.metadata!= null)&&this.metadata.equals(rhs.metadata))))&&((this.snapshotId == rhs.snapshotId)||((this.snapshotId!= null)&&this.snapshotId.equals(rhs.snapshotId))))&&((this.matchedProfileId == rhs.matchedProfileId)||((this.matchedProfileId!= null)&&this.matchedProfileId.equals(rhs.matchedProfileId))))&&((this.recordType == rhs.recordType)||((this.recordType!= null)&&this.recordType.equals(rhs.recordType))))&&((this.rawRecordId == rhs.rawRecordId)||((this.rawRecordId!= null)&&this.rawRecordId.equals(rhs.rawRecordId))))&&((this.parsedRecordId == rhs.parsedRecordId)||((this.parsedRecordId!= null)&&this.parsedRecordId.equals(rhs.parsedRecordId))))&&((this.externalIdsHolder == rhs.externalIdsHolder)||((this.externalIdsHolder!= null)&&this.externalIdsHolder.equals(rhs.externalIdsHolder))))&&((this.deleted == rhs.deleted)||((this.deleted!= null)&&this.deleted.equals(rhs.deleted))))&&((this.additionalInfo == rhs.additionalInfo)||((this.additionalInfo!= null)&&this.additionalInfo.equals(rhs.additionalInfo))))&&((this.errorRecordId == rhs.errorRecordId)||((this.errorRecordId!= null)&&this.errorRecordId.equals(rhs.errorRecordId))))&&((this.id == rhs.id)||((this.id!= null)&&this.id.equals(rhs.id))))&&((this.matchedId == rhs.matchedId)||((this.matchedId!= null)&&this.matchedId.equals(rhs.matchedId))))&&((this.order == rhs.order)||((this.order!= null)&&this.order.equals(rhs.order))));
    }


    /**
     * Record Type Enum
     * 
     */
    public enum RecordType {

        MARC("MARC");
        private final String value;
        private final static Map<String, RecordModel.RecordType> CONSTANTS = new HashMap<String, RecordModel.RecordType>();

        static {
            for (RecordModel.RecordType c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private RecordType(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }

        @JsonValue
        public String value() {
            return this.value;
        }

        @JsonCreator
        public static RecordModel.RecordType fromValue(String value) {
            RecordModel.RecordType constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
