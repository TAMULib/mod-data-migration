
package org.folio.rest.migration.model.generated.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


/**
 * Contains identifiers of external entities (instance, holding)
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "instanceId"
})
public class ExternalIdsHolder {

    /**
     * Regexp pattern for UUID validation
     * 
     */
    @JsonProperty("instanceId")
    @JsonPropertyDescription("Regexp pattern for UUID validation")
    private String instanceId;

    /**
     * Regexp pattern for UUID validation
     * 
     */
    @JsonProperty("instanceId")
    public String getInstanceId() {
        return instanceId;
    }

    /**
     * Regexp pattern for UUID validation
     * 
     */
    @JsonProperty("instanceId")
    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(ExternalIdsHolder.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("instanceId");
        sb.append('=');
        sb.append(((this.instanceId == null)?"<null>":this.instanceId));
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
        result = ((result* 31)+((this.instanceId == null)? 0 :this.instanceId.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ExternalIdsHolder) == false) {
            return false;
        }
        ExternalIdsHolder rhs = ((ExternalIdsHolder) other);
        return ((this.instanceId == rhs.instanceId)||((this.instanceId!= null)&&this.instanceId.equals(rhs.instanceId)));
    }

}
