
package org.folio.rest.migration.model.generated.inventory_storage;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "alternativeTitleTypeId",
    "alternativeTitle"
})
public class AlternativeTitle {

    /**
     * Regexp pattern for UUID validation
     * 
     */
    @JsonProperty("alternativeTitleTypeId")
    @JsonPropertyDescription("Regexp pattern for UUID validation")
    private String alternativeTitleTypeId;
    /**
     * An alternative title for the resource
     * 
     */
    @JsonProperty("alternativeTitle")
    @JsonPropertyDescription("An alternative title for the resource")
    private String alternativeTitle;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * Regexp pattern for UUID validation
     * 
     */
    @JsonProperty("alternativeTitleTypeId")
    public String getAlternativeTitleTypeId() {
        return alternativeTitleTypeId;
    }

    /**
     * Regexp pattern for UUID validation
     * 
     */
    @JsonProperty("alternativeTitleTypeId")
    public void setAlternativeTitleTypeId(String alternativeTitleTypeId) {
        this.alternativeTitleTypeId = alternativeTitleTypeId;
    }

    /**
     * An alternative title for the resource
     * 
     */
    @JsonProperty("alternativeTitle")
    public String getAlternativeTitle() {
        return alternativeTitle;
    }

    /**
     * An alternative title for the resource
     * 
     */
    @JsonProperty("alternativeTitle")
    public void setAlternativeTitle(String alternativeTitle) {
        this.alternativeTitle = alternativeTitle;
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
        sb.append(AlternativeTitle.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("alternativeTitleTypeId");
        sb.append('=');
        sb.append(((this.alternativeTitleTypeId == null)?"<null>":this.alternativeTitleTypeId));
        sb.append(',');
        sb.append("alternativeTitle");
        sb.append('=');
        sb.append(((this.alternativeTitle == null)?"<null>":this.alternativeTitle));
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
        result = ((result* 31)+((this.alternativeTitle == null)? 0 :this.alternativeTitle.hashCode()));
        result = ((result* 31)+((this.additionalProperties == null)? 0 :this.additionalProperties.hashCode()));
        result = ((result* 31)+((this.alternativeTitleTypeId == null)? 0 :this.alternativeTitleTypeId.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof AlternativeTitle) == false) {
            return false;
        }
        AlternativeTitle rhs = ((AlternativeTitle) other);
        return ((((this.alternativeTitle == rhs.alternativeTitle)||((this.alternativeTitle!= null)&&this.alternativeTitle.equals(rhs.alternativeTitle)))&&((this.additionalProperties == rhs.additionalProperties)||((this.additionalProperties!= null)&&this.additionalProperties.equals(rhs.additionalProperties))))&&((this.alternativeTitleTypeId == rhs.alternativeTitleTypeId)||((this.alternativeTitleTypeId!= null)&&this.alternativeTitleTypeId.equals(rhs.alternativeTitleTypeId))));
    }

}
