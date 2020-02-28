
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
 * A collection of identifier types
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "identifierTypes",
    "totalRecords"
})
public class IdentifierTypes {

    /**
     * List of identifier types
     * (Required)
     * 
     */
    @JsonProperty("identifierTypes")
    @JsonPropertyDescription("List of identifier types")
    private List<IdentifierType> identifierTypes = new ArrayList<IdentifierType>();
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
     * List of identifier types
     * (Required)
     * 
     */
    @JsonProperty("identifierTypes")
    public List<IdentifierType> getIdentifierTypes() {
        return identifierTypes;
    }

    /**
     * List of identifier types
     * (Required)
     * 
     */
    @JsonProperty("identifierTypes")
    public void setIdentifierTypes(List<IdentifierType> identifierTypes) {
        this.identifierTypes = identifierTypes;
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
        sb.append(IdentifierTypes.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("identifierTypes");
        sb.append('=');
        sb.append(((this.identifierTypes == null)?"<null>":this.identifierTypes));
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
        result = ((result* 31)+((this.identifierTypes == null)? 0 :this.identifierTypes.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof IdentifierTypes) == false) {
            return false;
        }
        IdentifierTypes rhs = ((IdentifierTypes) other);
        return ((((this.totalRecords == rhs.totalRecords)||((this.totalRecords!= null)&&this.totalRecords.equals(rhs.totalRecords)))&&((this.additionalProperties == rhs.additionalProperties)||((this.additionalProperties!= null)&&this.additionalProperties.equals(rhs.additionalProperties))))&&((this.identifierTypes == rhs.identifierTypes)||((this.identifierTypes!= null)&&this.identifierTypes.equals(rhs.identifierTypes))));
    }

}
