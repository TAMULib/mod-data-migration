
package org.folio.rest.migration.model.generated.settings;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.folio.rest.migration.model.generated.inventory_storage.Metadata;


/**
 * A contributor name type
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "name",
    "ordering",
    "metadata"
})
public class ContributorNameType {

    @JsonProperty("id")
    private String id;
    /**
     * label for the type of contributor name
     * (Required)
     * 
     */
    @JsonProperty("name")
    @JsonPropertyDescription("label for the type of contributor name")
    private String name;
    /**
     * used for ordering of contributor name types in displays, i.e. in select lists
     * 
     */
    @JsonProperty("ordering")
    @JsonPropertyDescription("used for ordering of contributor name types in displays, i.e. in select lists")
    private String ordering;
    /**
     * Metadata Schema
     * <p>
     * Metadata about creation and changes to records, provided by the server (client should not provide)
     * 
     */
    @JsonProperty("metadata")
    @JsonPropertyDescription("Metadata about creation and changes to records, provided by the server (client should not provide)")
    private Metadata metadata;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    /**
     * label for the type of contributor name
     * (Required)
     * 
     */
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    /**
     * label for the type of contributor name
     * (Required)
     * 
     */
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    /**
     * used for ordering of contributor name types in displays, i.e. in select lists
     * 
     */
    @JsonProperty("ordering")
    public String getOrdering() {
        return ordering;
    }

    /**
     * used for ordering of contributor name types in displays, i.e. in select lists
     * 
     */
    @JsonProperty("ordering")
    public void setOrdering(String ordering) {
        this.ordering = ordering;
    }

    /**
     * Metadata Schema
     * <p>
     * Metadata about creation and changes to records, provided by the server (client should not provide)
     * 
     */
    @JsonProperty("metadata")
    public Metadata getMetadata() {
        return metadata;
    }

    /**
     * Metadata Schema
     * <p>
     * Metadata about creation and changes to records, provided by the server (client should not provide)
     * 
     */
    @JsonProperty("metadata")
    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
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
        sb.append(ContributorNameType.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("id");
        sb.append('=');
        sb.append(((this.id == null)?"<null>":this.id));
        sb.append(',');
        sb.append("name");
        sb.append('=');
        sb.append(((this.name == null)?"<null>":this.name));
        sb.append(',');
        sb.append("ordering");
        sb.append('=');
        sb.append(((this.ordering == null)?"<null>":this.ordering));
        sb.append(',');
        sb.append("metadata");
        sb.append('=');
        sb.append(((this.metadata == null)?"<null>":this.metadata));
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
        result = ((result* 31)+((this.name == null)? 0 :this.name.hashCode()));
        result = ((result* 31)+((this.metadata == null)? 0 :this.metadata.hashCode()));
        result = ((result* 31)+((this.id == null)? 0 :this.id.hashCode()));
        result = ((result* 31)+((this.additionalProperties == null)? 0 :this.additionalProperties.hashCode()));
        result = ((result* 31)+((this.ordering == null)? 0 :this.ordering.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ContributorNameType) == false) {
            return false;
        }
        ContributorNameType rhs = ((ContributorNameType) other);
        return ((((((this.name == rhs.name)||((this.name!= null)&&this.name.equals(rhs.name)))&&((this.metadata == rhs.metadata)||((this.metadata!= null)&&this.metadata.equals(rhs.metadata))))&&((this.id == rhs.id)||((this.id!= null)&&this.id.equals(rhs.id))))&&((this.additionalProperties == rhs.additionalProperties)||((this.additionalProperties!= null)&&this.additionalProperties.equals(rhs.additionalProperties))))&&((this.ordering == rhs.ordering)||((this.ordering!= null)&&this.ordering.equals(rhs.ordering))));
    }

}
