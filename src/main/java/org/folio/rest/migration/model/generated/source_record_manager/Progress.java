
package org.folio.rest.migration.model.generated.source_record_manager;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


/**
 * Execution progress of the job
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "jobExecutionId",
    "current",
    "total"
})
public class Progress {

    /**
     * Corresponding jobExecution id
     * 
     */
    @JsonProperty("jobExecutionId")
    @JsonPropertyDescription("Corresponding jobExecution id")
    private String jobExecutionId;
    /**
     * Currently processing record
     * 
     */
    @JsonProperty("current")
    @JsonPropertyDescription("Currently processing record")
    private Integer current;
    /**
     * Total number of records to be processed
     * 
     */
    @JsonProperty("total")
    @JsonPropertyDescription("Total number of records to be processed")
    private Integer total;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * Corresponding jobExecution id
     * 
     */
    @JsonProperty("jobExecutionId")
    public String getJobExecutionId() {
        return jobExecutionId;
    }

    /**
     * Corresponding jobExecution id
     * 
     */
    @JsonProperty("jobExecutionId")
    public void setJobExecutionId(String jobExecutionId) {
        this.jobExecutionId = jobExecutionId;
    }

    /**
     * Currently processing record
     * 
     */
    @JsonProperty("current")
    public Integer getCurrent() {
        return current;
    }

    /**
     * Currently processing record
     * 
     */
    @JsonProperty("current")
    public void setCurrent(Integer current) {
        this.current = current;
    }

    /**
     * Total number of records to be processed
     * 
     */
    @JsonProperty("total")
    public Integer getTotal() {
        return total;
    }

    /**
     * Total number of records to be processed
     * 
     */
    @JsonProperty("total")
    public void setTotal(Integer total) {
        this.total = total;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Progress.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("jobExecutionId");
        sb.append('=');
        sb.append(((this.jobExecutionId == null)?"<null>":this.jobExecutionId));
        sb.append(',');
        sb.append("current");
        sb.append('=');
        sb.append(((this.current == null)?"<null>":this.current));
        sb.append(',');
        sb.append("total");
        sb.append('=');
        sb.append(((this.total == null)?"<null>":this.total));
        sb.append(',');
        sb.append("additionalProperties");
        sb.append('=');
        sb.append(((this.additionalProperties == null)?"<null>":this.additionalProperties));
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
        result = ((result* 31)+((this.current == null)? 0 :this.current.hashCode()));
        result = ((result* 31)+((this.total == null)? 0 :this.total.hashCode()));
        result = ((result* 31)+((this.additionalProperties == null)? 0 :this.additionalProperties.hashCode()));
        result = ((result* 31)+((this.jobExecutionId == null)? 0 :this.jobExecutionId.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Progress) == false) {
            return false;
        }
        Progress rhs = ((Progress) other);
        return (((((this.current == rhs.current)||((this.current!= null)&&this.current.equals(rhs.current)))&&((this.total == rhs.total)||((this.total!= null)&&this.total.equals(rhs.total))))&&((this.additionalProperties == rhs.additionalProperties)||((this.additionalProperties!= null)&&this.additionalProperties.equals(rhs.additionalProperties))))&&((this.jobExecutionId == rhs.jobExecutionId)||((this.jobExecutionId!= null)&&this.jobExecutionId.equals(rhs.jobExecutionId))));
    }

}
