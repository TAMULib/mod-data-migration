
package org.folio.rest.migration.model.generated.source_record_manager;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;


/**
 * Job Execution Source Chunk Schema
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "jobExecutionId",
    "last",
    "state",
    "chunkSize",
    "processedAmount",
    "createdDate",
    "completedDate",
    "error"
})
public class JobExecutionSourceChunk {

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
    @JsonProperty("jobExecutionId")
    @JsonPropertyDescription("Regexp pattern for UUID validation")
    private String jobExecutionId;
    /**
     * True if this is the last chunk False otherwise
     * (Required)
     * 
     */
    @JsonProperty("last")
    @JsonPropertyDescription("True if this is the last chunk False otherwise")
    private Boolean last;
    /**
     * Represents the current state of the chunk processing, possible values: InProgress, Completed, Error
     * (Required)
     * 
     */
    @JsonProperty("state")
    @JsonPropertyDescription("Represents the current state of the chunk processing, possible values: InProgress, Completed, Error")
    private JobExecutionSourceChunk.State state;
    /**
     * The total number of records in the chunk
     * 
     */
    @JsonProperty("chunkSize")
    @JsonPropertyDescription("The total number of records in the chunk")
    private Integer chunkSize;
    /**
     * The number of records which have been processed already
     * 
     */
    @JsonProperty("processedAmount")
    @JsonPropertyDescription("The number of records which have been processed already")
    private Integer processedAmount = 0;
    /**
     * Instant when chunk processing was started
     * 
     */
    @JsonProperty("createdDate")
    @JsonPropertyDescription("Instant when chunk processing was started")
    private Date createdDate;
    /**
     * Instant when chunk processing was completed
     * 
     */
    @JsonProperty("completedDate")
    @JsonPropertyDescription("Instant when chunk processing was completed")
    private Date completedDate;
    /**
     * Contains an error message and trace stack if the chunk processing has failed, makes sense only if State == Error
     * 
     */
    @JsonProperty("error")
    @JsonPropertyDescription("Contains an error message and trace stack if the chunk processing has failed, makes sense only if State == Error")
    private String error;

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
     * True if this is the last chunk False otherwise
     * (Required)
     * 
     */
    @JsonProperty("last")
    public Boolean getLast() {
        return last;
    }

    /**
     * True if this is the last chunk False otherwise
     * (Required)
     * 
     */
    @JsonProperty("last")
    public void setLast(Boolean last) {
        this.last = last;
    }

    /**
     * Represents the current state of the chunk processing, possible values: InProgress, Completed, Error
     * (Required)
     * 
     */
    @JsonProperty("state")
    public JobExecutionSourceChunk.State getState() {
        return state;
    }

    /**
     * Represents the current state of the chunk processing, possible values: InProgress, Completed, Error
     * (Required)
     * 
     */
    @JsonProperty("state")
    public void setState(JobExecutionSourceChunk.State state) {
        this.state = state;
    }

    /**
     * The total number of records in the chunk
     * 
     */
    @JsonProperty("chunkSize")
    public Integer getChunkSize() {
        return chunkSize;
    }

    /**
     * The total number of records in the chunk
     * 
     */
    @JsonProperty("chunkSize")
    public void setChunkSize(Integer chunkSize) {
        this.chunkSize = chunkSize;
    }

    /**
     * The number of records which have been processed already
     * 
     */
    @JsonProperty("processedAmount")
    public Integer getProcessedAmount() {
        return processedAmount;
    }

    /**
     * The number of records which have been processed already
     * 
     */
    @JsonProperty("processedAmount")
    public void setProcessedAmount(Integer processedAmount) {
        this.processedAmount = processedAmount;
    }

    /**
     * Instant when chunk processing was started
     * 
     */
    @JsonProperty("createdDate")
    public Date getCreatedDate() {
        return createdDate;
    }

    /**
     * Instant when chunk processing was started
     * 
     */
    @JsonProperty("createdDate")
    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    /**
     * Instant when chunk processing was completed
     * 
     */
    @JsonProperty("completedDate")
    public Date getCompletedDate() {
        return completedDate;
    }

    /**
     * Instant when chunk processing was completed
     * 
     */
    @JsonProperty("completedDate")
    public void setCompletedDate(Date completedDate) {
        this.completedDate = completedDate;
    }

    /**
     * Contains an error message and trace stack if the chunk processing has failed, makes sense only if State == Error
     * 
     */
    @JsonProperty("error")
    public String getError() {
        return error;
    }

    /**
     * Contains an error message and trace stack if the chunk processing has failed, makes sense only if State == Error
     * 
     */
    @JsonProperty("error")
    public void setError(String error) {
        this.error = error;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(JobExecutionSourceChunk.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("id");
        sb.append('=');
        sb.append(((this.id == null)?"<null>":this.id));
        sb.append(',');
        sb.append("jobExecutionId");
        sb.append('=');
        sb.append(((this.jobExecutionId == null)?"<null>":this.jobExecutionId));
        sb.append(',');
        sb.append("last");
        sb.append('=');
        sb.append(((this.last == null)?"<null>":this.last));
        sb.append(',');
        sb.append("state");
        sb.append('=');
        sb.append(((this.state == null)?"<null>":this.state));
        sb.append(',');
        sb.append("chunkSize");
        sb.append('=');
        sb.append(((this.chunkSize == null)?"<null>":this.chunkSize));
        sb.append(',');
        sb.append("processedAmount");
        sb.append('=');
        sb.append(((this.processedAmount == null)?"<null>":this.processedAmount));
        sb.append(',');
        sb.append("createdDate");
        sb.append('=');
        sb.append(((this.createdDate == null)?"<null>":this.createdDate));
        sb.append(',');
        sb.append("completedDate");
        sb.append('=');
        sb.append(((this.completedDate == null)?"<null>":this.completedDate));
        sb.append(',');
        sb.append("error");
        sb.append('=');
        sb.append(((this.error == null)?"<null>":this.error));
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
        result = ((result* 31)+((this.createdDate == null)? 0 :this.createdDate.hashCode()));
        result = ((result* 31)+((this.jobExecutionId == null)? 0 :this.jobExecutionId.hashCode()));
        result = ((result* 31)+((this.last == null)? 0 :this.last.hashCode()));
        result = ((result* 31)+((this.chunkSize == null)? 0 :this.chunkSize.hashCode()));
        result = ((result* 31)+((this.processedAmount == null)? 0 :this.processedAmount.hashCode()));
        result = ((result* 31)+((this.id == null)? 0 :this.id.hashCode()));
        result = ((result* 31)+((this.state == null)? 0 :this.state.hashCode()));
        result = ((result* 31)+((this.error == null)? 0 :this.error.hashCode()));
        result = ((result* 31)+((this.completedDate == null)? 0 :this.completedDate.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof JobExecutionSourceChunk) == false) {
            return false;
        }
        JobExecutionSourceChunk rhs = ((JobExecutionSourceChunk) other);
        return ((((((((((this.createdDate == rhs.createdDate)||((this.createdDate!= null)&&this.createdDate.equals(rhs.createdDate)))&&((this.jobExecutionId == rhs.jobExecutionId)||((this.jobExecutionId!= null)&&this.jobExecutionId.equals(rhs.jobExecutionId))))&&((this.last == rhs.last)||((this.last!= null)&&this.last.equals(rhs.last))))&&((this.chunkSize == rhs.chunkSize)||((this.chunkSize!= null)&&this.chunkSize.equals(rhs.chunkSize))))&&((this.processedAmount == rhs.processedAmount)||((this.processedAmount!= null)&&this.processedAmount.equals(rhs.processedAmount))))&&((this.id == rhs.id)||((this.id!= null)&&this.id.equals(rhs.id))))&&((this.state == rhs.state)||((this.state!= null)&&this.state.equals(rhs.state))))&&((this.error == rhs.error)||((this.error!= null)&&this.error.equals(rhs.error))))&&((this.completedDate == rhs.completedDate)||((this.completedDate!= null)&&this.completedDate.equals(rhs.completedDate))));
    }


    /**
     * Represents the current state of the chunk processing, possible values: InProgress, Completed, Error
     * 
     */
    public enum State {

        IN_PROGRESS("IN_PROGRESS"),
        COMPLETED("COMPLETED"),
        ERROR("ERROR");
        private final String value;
        private final static Map<String, JobExecutionSourceChunk.State> CONSTANTS = new HashMap<String, JobExecutionSourceChunk.State>();

        static {
            for (JobExecutionSourceChunk.State c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private State(String value) {
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
        public static JobExecutionSourceChunk.State fromValue(String value) {
            JobExecutionSourceChunk.State constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
