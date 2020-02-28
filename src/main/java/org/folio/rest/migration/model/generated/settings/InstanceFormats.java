
package org.folio.rest.migration.model.generated.settings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


/**
 * A collection of Instance format records (controlled vocabulary)
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "instanceFormats",
    "totalRecords"
})
public class InstanceFormats {

    /**
     * List of instance formats
     * (Required)
     * 
     */
    @JsonProperty("instanceFormats")
    @JsonPropertyDescription("List of instance formats")
    private List<InstanceFormat> instanceFormats = new ArrayList<InstanceFormat>();
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("totalRecords")
    private Integer totalRecords;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * List of instance formats
     * (Required)
     * 
     */
    @JsonProperty("instanceFormats")
    public List<InstanceFormat> getInstanceFormats() {
        return instanceFormats;
    }

    /**
     * List of instance formats
     * (Required)
     * 
     */
    @JsonProperty("instanceFormats")
    public void setInstanceFormats(List<InstanceFormat> instanceFormats) {
        this.instanceFormats = instanceFormats;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("totalRecords")
    public Integer getTotalRecords() {
        return totalRecords;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("totalRecords")
    public void setTotalRecords(Integer totalRecords) {
        this.totalRecords = totalRecords;
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
        sb.append(InstanceFormats.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("instanceFormats");
        sb.append('=');
        sb.append(((this.instanceFormats == null)?"<null>":this.instanceFormats));
        sb.append(',');
        sb.append("totalRecords");
        sb.append('=');
        sb.append(((this.totalRecords == null)?"<null>":this.totalRecords));
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
        result = ((result* 31)+((this.totalRecords == null)? 0 :this.totalRecords.hashCode()));
        result = ((result* 31)+((this.additionalProperties == null)? 0 :this.additionalProperties.hashCode()));
        result = ((result* 31)+((this.instanceFormats == null)? 0 :this.instanceFormats.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof InstanceFormats) == false) {
            return false;
        }
        InstanceFormats rhs = ((InstanceFormats) other);
        return ((((this.totalRecords == rhs.totalRecords)||((this.totalRecords!= null)&&this.totalRecords.equals(rhs.totalRecords)))&&((this.additionalProperties == rhs.additionalProperties)||((this.additionalProperties!= null)&&this.additionalProperties.equals(rhs.additionalProperties))))&&((this.instanceFormats == rhs.instanceFormats)||((this.instanceFormats!= null)&&this.instanceFormats.equals(rhs.instanceFormats))));
    }

}
