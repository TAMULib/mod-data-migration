
package org.folio.rest.migration.model.generated.inventory_storage;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "value",
    "identifierTypeId"
})
public class Identifier {

    /**
     * Resource identifier value
     * (Required)
     * 
     */
    @JsonProperty("value")
    @JsonPropertyDescription("Resource identifier value")
    private String value;
    /**
     * Regexp pattern for UUID validation
     * (Required)
     * 
     */
    @JsonProperty("identifierTypeId")
    @JsonPropertyDescription("Regexp pattern for UUID validation")
    private String identifierTypeId;

    /**
     * Resource identifier value
     * (Required)
     * 
     */
    @JsonProperty("value")
    public String getValue() {
        return value;
    }

    /**
     * Resource identifier value
     * (Required)
     * 
     */
    @JsonProperty("value")
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Regexp pattern for UUID validation
     * (Required)
     * 
     */
    @JsonProperty("identifierTypeId")
    public String getIdentifierTypeId() {
        return identifierTypeId;
    }

    /**
     * Regexp pattern for UUID validation
     * (Required)
     * 
     */
    @JsonProperty("identifierTypeId")
    public void setIdentifierTypeId(String identifierTypeId) {
        this.identifierTypeId = identifierTypeId;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Identifier.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("value");
        sb.append('=');
        sb.append(((this.value == null)?"<null>":this.value));
        sb.append(',');
        sb.append("identifierTypeId");
        sb.append('=');
        sb.append(((this.identifierTypeId == null)?"<null>":this.identifierTypeId));
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
        result = ((result* 31)+((this.value == null)? 0 :this.value.hashCode()));
        result = ((result* 31)+((this.identifierTypeId == null)? 0 :this.identifierTypeId.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Identifier) == false) {
            return false;
        }
        Identifier rhs = ((Identifier) other);
        return (((this.value == rhs.value)||((this.value!= null)&&this.value.equals(rhs.value)))&&((this.identifierTypeId == rhs.identifierTypeId)||((this.identifierTypeId!= null)&&this.identifierTypeId.equals(rhs.identifierTypeId))));
    }

}
