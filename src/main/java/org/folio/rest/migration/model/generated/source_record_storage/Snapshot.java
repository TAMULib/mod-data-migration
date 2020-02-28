
package org.folio.rest.migration.model.generated.source_record_storage;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.folio.rest.migration.model.generated.common.Status;
import org.folio.rest.migration.model.generated.inventory_storage.Metadata;


/**
 * Snapshot Schema
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "jobExecutionId",
    "status",
    "processingStartedDate",
    "metadata"
})
public class Snapshot {

    /**
     * Regexp pattern for UUID validation
     * (Required)
     * 
     */
    @JsonProperty("jobExecutionId")
    @JsonPropertyDescription("Regexp pattern for UUID validation")
    private String jobExecutionId;
    /**
     * JobExecution status Enum
     * (Required)
     * 
     */
    @JsonProperty("status")
    @JsonPropertyDescription("JobExecution status Enum")
    private Status status;
    /**
     * Date and time when parsing of records started, set when status is updated to PARSING_IN_PROGRESS
     * 
     */
    @JsonProperty("processingStartedDate")
    @JsonPropertyDescription("Date and time when parsing of records started, set when status is updated to PARSING_IN_PROGRESS")
    private Date processingStartedDate;
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
     * (Required)
     * 
     */
    @JsonProperty("jobExecutionId")
    public String getJobExecutionId() {
        return jobExecutionId;
    }

    /**
     * Regexp pattern for UUID validation
     * (Required)
     * 
     */
    @JsonProperty("jobExecutionId")
    public void setJobExecutionId(String jobExecutionId) {
        this.jobExecutionId = jobExecutionId;
    }

    /**
     * JobExecution status Enum
     * (Required)
     * 
     */
    @JsonProperty("status")
    public Status getStatus() {
        return status;
    }

    /**
     * JobExecution status Enum
     * (Required)
     * 
     */
    @JsonProperty("status")
    public void setStatus(Status status) {
        this.status = status;
    }

    /**
     * Date and time when parsing of records started, set when status is updated to PARSING_IN_PROGRESS
     * 
     */
    @JsonProperty("processingStartedDate")
    public Date getProcessingStartedDate() {
        return processingStartedDate;
    }

    /**
     * Date and time when parsing of records started, set when status is updated to PARSING_IN_PROGRESS
     * 
     */
    @JsonProperty("processingStartedDate")
    public void setProcessingStartedDate(Date processingStartedDate) {
        this.processingStartedDate = processingStartedDate;
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
        sb.append(Snapshot.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("jobExecutionId");
        sb.append('=');
        sb.append(((this.jobExecutionId == null)?"<null>":this.jobExecutionId));
        sb.append(',');
        sb.append("status");
        sb.append('=');
        sb.append(((this.status == null)?"<null>":this.status));
        sb.append(',');
        sb.append("processingStartedDate");
        sb.append('=');
        sb.append(((this.processingStartedDate == null)?"<null>":this.processingStartedDate));
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
        result = ((result* 31)+((this.metadata == null)? 0 :this.metadata.hashCode()));
        result = ((result* 31)+((this.jobExecutionId == null)? 0 :this.jobExecutionId.hashCode()));
        result = ((result* 31)+((this.processingStartedDate == null)? 0 :this.processingStartedDate.hashCode()));
        result = ((result* 31)+((this.status == null)? 0 :this.status.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Snapshot) == false) {
            return false;
        }
        Snapshot rhs = ((Snapshot) other);
        return (((((this.metadata == rhs.metadata)||((this.metadata!= null)&&this.metadata.equals(rhs.metadata)))&&((this.jobExecutionId == rhs.jobExecutionId)||((this.jobExecutionId!= null)&&this.jobExecutionId.equals(rhs.jobExecutionId))))&&((this.processingStartedDate == rhs.processingStartedDate)||((this.processingStartedDate!= null)&&this.processingStartedDate.equals(rhs.processingStartedDate))))&&((this.status == rhs.status)||((this.status!= null)&&this.status.equals(rhs.status))));
    }

}
