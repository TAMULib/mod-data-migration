
package org.folio.rest.migration.model.generated.source_record_manager;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


/**
 * Initialized JobExecution entities
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "parentJobExecutionId",
    "jobExecutions"
})
public class InitJobExecutionsRsDto {

    /**
     * Regexp pattern for UUID validation
     * 
     */
    @JsonProperty("parentJobExecutionId")
    @JsonPropertyDescription("Regexp pattern for UUID validation")
    private String parentJobExecutionId;
    /**
     * Array of initialized JobExecution entities
     * 
     */
    @JsonProperty("jobExecutions")
    @JsonPropertyDescription("Array of initialized JobExecution entities")
    private List<JobExecution> jobExecutions = new ArrayList<JobExecution>();

    /**
     * Regexp pattern for UUID validation
     * 
     */
    @JsonProperty("parentJobExecutionId")
    public String getParentJobExecutionId() {
        return parentJobExecutionId;
    }

    /**
     * Regexp pattern for UUID validation
     * 
     */
    @JsonProperty("parentJobExecutionId")
    public void setParentJobExecutionId(String parentJobExecutionId) {
        this.parentJobExecutionId = parentJobExecutionId;
    }

    /**
     * Array of initialized JobExecution entities
     * 
     */
    @JsonProperty("jobExecutions")
    public List<JobExecution> getJobExecutions() {
        return jobExecutions;
    }

    /**
     * Array of initialized JobExecution entities
     * 
     */
    @JsonProperty("jobExecutions")
    public void setJobExecutions(List<JobExecution> jobExecutions) {
        this.jobExecutions = jobExecutions;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(InitJobExecutionsRsDto.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("parentJobExecutionId");
        sb.append('=');
        sb.append(((this.parentJobExecutionId == null)?"<null>":this.parentJobExecutionId));
        sb.append(',');
        sb.append("jobExecutions");
        sb.append('=');
        sb.append(((this.jobExecutions == null)?"<null>":this.jobExecutions));
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
        result = ((result* 31)+((this.jobExecutions == null)? 0 :this.jobExecutions.hashCode()));
        result = ((result* 31)+((this.parentJobExecutionId == null)? 0 :this.parentJobExecutionId.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof InitJobExecutionsRsDto) == false) {
            return false;
        }
        InitJobExecutionsRsDto rhs = ((InitJobExecutionsRsDto) other);
        return (((this.jobExecutions == rhs.jobExecutions)||((this.jobExecutions!= null)&&this.jobExecutions.equals(rhs.jobExecutions)))&&((this.parentJobExecutionId == rhs.parentJobExecutionId)||((this.parentJobExecutionId!= null)&&this.parentJobExecutionId.equals(rhs.parentJobExecutionId))));
    }

}
