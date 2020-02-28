
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
 * A collection of Instance note types
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "instanceNoteTypes",
    "totalRecords"
})
public class InstanceNoteTypes {

    /**
     * List of Instance note types
     * (Required)
     * 
     */
    @JsonProperty("instanceNoteTypes")
    @JsonPropertyDescription("List of Instance note types")
    private List<InstanceNoteType> instanceNoteTypes = new ArrayList<InstanceNoteType>();
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
     * List of Instance note types
     * (Required)
     * 
     */
    @JsonProperty("instanceNoteTypes")
    public List<InstanceNoteType> getInstanceNoteTypes() {
        return instanceNoteTypes;
    }

    /**
     * List of Instance note types
     * (Required)
     * 
     */
    @JsonProperty("instanceNoteTypes")
    public void setInstanceNoteTypes(List<InstanceNoteType> instanceNoteTypes) {
        this.instanceNoteTypes = instanceNoteTypes;
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
        sb.append(InstanceNoteTypes.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("instanceNoteTypes");
        sb.append('=');
        sb.append(((this.instanceNoteTypes == null)?"<null>":this.instanceNoteTypes));
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
        result = ((result* 31)+((this.instanceNoteTypes == null)? 0 :this.instanceNoteTypes.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof InstanceNoteTypes) == false) {
            return false;
        }
        InstanceNoteTypes rhs = ((InstanceNoteTypes) other);
        return ((((this.totalRecords == rhs.totalRecords)||((this.totalRecords!= null)&&this.totalRecords.equals(rhs.totalRecords)))&&((this.additionalProperties == rhs.additionalProperties)||((this.additionalProperties!= null)&&this.additionalProperties.equals(rhs.additionalProperties))))&&((this.instanceNoteTypes == rhs.instanceNoteTypes)||((this.instanceNoteTypes!= null)&&this.instanceNoteTypes.equals(rhs.instanceNoteTypes))));
    }

}
