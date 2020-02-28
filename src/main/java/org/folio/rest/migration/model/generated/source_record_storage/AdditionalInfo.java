
package org.folio.rest.migration.model.generated.source_record_storage;

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
 * Auxiliary data which is not related to MARC type record
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "suppressDiscovery"
})
public class AdditionalInfo {

    /**
     * Flag indicates if the record is displayed during a search
     * 
     */
    @JsonProperty("suppressDiscovery")
    @JsonPropertyDescription("Flag indicates if the record is displayed during a search")
    private Boolean suppressDiscovery = false;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * Flag indicates if the record is displayed during a search
     * 
     */
    @JsonProperty("suppressDiscovery")
    public Boolean getSuppressDiscovery() {
        return suppressDiscovery;
    }

    /**
     * Flag indicates if the record is displayed during a search
     * 
     */
    @JsonProperty("suppressDiscovery")
    public void setSuppressDiscovery(Boolean suppressDiscovery) {
        this.suppressDiscovery = suppressDiscovery;
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
        sb.append(AdditionalInfo.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("suppressDiscovery");
        sb.append('=');
        sb.append(((this.suppressDiscovery == null)?"<null>":this.suppressDiscovery));
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
        result = ((result* 31)+((this.suppressDiscovery == null)? 0 :this.suppressDiscovery.hashCode()));
        result = ((result* 31)+((this.additionalProperties == null)? 0 :this.additionalProperties.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof AdditionalInfo) == false) {
            return false;
        }
        AdditionalInfo rhs = ((AdditionalInfo) other);
        return (((this.suppressDiscovery == rhs.suppressDiscovery)||((this.suppressDiscovery!= null)&&this.suppressDiscovery.equals(rhs.suppressDiscovery)))&&((this.additionalProperties == rhs.additionalProperties)||((this.additionalProperties!= null)&&this.additionalProperties.equals(rhs.additionalProperties))));
    }

}
