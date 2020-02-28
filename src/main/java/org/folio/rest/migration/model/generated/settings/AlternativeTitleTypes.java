
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
 * A collection of alternative title types
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "alternativeTitleTypes",
    "totalRecords"
})
public class AlternativeTitleTypes {

    /**
     * List of alternative title types
     * (Required)
     * 
     */
    @JsonProperty("alternativeTitleTypes")
    @JsonPropertyDescription("List of alternative title types")
    private List<AlternativeTitleType> alternativeTitleTypes = new ArrayList<AlternativeTitleType>();
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
     * List of alternative title types
     * (Required)
     * 
     */
    @JsonProperty("alternativeTitleTypes")
    public List<AlternativeTitleType> getAlternativeTitleTypes() {
        return alternativeTitleTypes;
    }

    /**
     * List of alternative title types
     * (Required)
     * 
     */
    @JsonProperty("alternativeTitleTypes")
    public void setAlternativeTitleTypes(List<AlternativeTitleType> alternativeTitleTypes) {
        this.alternativeTitleTypes = alternativeTitleTypes;
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
        sb.append(AlternativeTitleTypes.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("alternativeTitleTypes");
        sb.append('=');
        sb.append(((this.alternativeTitleTypes == null)?"<null>":this.alternativeTitleTypes));
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
        result = ((result* 31)+((this.alternativeTitleTypes == null)? 0 :this.alternativeTitleTypes.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof AlternativeTitleTypes) == false) {
            return false;
        }
        AlternativeTitleTypes rhs = ((AlternativeTitleTypes) other);
        return ((((this.totalRecords == rhs.totalRecords)||((this.totalRecords!= null)&&this.totalRecords.equals(rhs.totalRecords)))&&((this.additionalProperties == rhs.additionalProperties)||((this.additionalProperties!= null)&&this.additionalProperties.equals(rhs.additionalProperties))))&&((this.alternativeTitleTypes == rhs.alternativeTitleTypes)||((this.alternativeTitleTypes!= null)&&this.alternativeTitleTypes.equals(rhs.alternativeTitleTypes))));
    }

}
