
package org.folio.rest.migration.model.generated.settings;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.folio.rest.migration.model.generated.inventory_storage.Metadata;


/**
 * A call number type
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "name",
    "source",
    "metadata"
})
public class AlternativeTitleType {

    /**
     * unique ID of the alternative title type; a UUID
     * 
     */
    @JsonProperty("id")
    @JsonPropertyDescription("unique ID of the alternative title type; a UUID")
    private String id;
    /**
     * name of the alternative title type
     * (Required)
     * 
     */
    @JsonProperty("name")
    @JsonPropertyDescription("name of the alternative title type")
    private String name;
    /**
     * label indicating where the alternative title type entry originates from, i.e. 'folio' or 'local'
     * (Required)
     * 
     */
    @JsonProperty("source")
    @JsonPropertyDescription("label indicating where the alternative title type entry originates from, i.e. 'folio' or 'local'")
    private String source;
    /**
     * Metadata Schema
     * <p>
     * Metadata about creation and changes to records, provided by the server (client should not provide)
     * 
     */
    @JsonProperty("metadata")
    @JsonPropertyDescription("Metadata about creation and changes to records, provided by the server (client should not provide)")
    private Metadata metadata;

    /**
     * unique ID of the alternative title type; a UUID
     * 
     */
    @JsonProperty("id")
    public String getId() {
        return id;
    }

    /**
     * unique ID of the alternative title type; a UUID
     * 
     */
    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    /**
     * name of the alternative title type
     * (Required)
     * 
     */
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    /**
     * name of the alternative title type
     * (Required)
     * 
     */
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    /**
     * label indicating where the alternative title type entry originates from, i.e. 'folio' or 'local'
     * (Required)
     * 
     */
    @JsonProperty("source")
    public String getSource() {
        return source;
    }

    /**
     * label indicating where the alternative title type entry originates from, i.e. 'folio' or 'local'
     * (Required)
     * 
     */
    @JsonProperty("source")
    public void setSource(String source) {
        this.source = source;
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(AlternativeTitleType.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("id");
        sb.append('=');
        sb.append(((this.id == null)?"<null>":this.id));
        sb.append(',');
        sb.append("name");
        sb.append('=');
        sb.append(((this.name == null)?"<null>":this.name));
        sb.append(',');
        sb.append("source");
        sb.append('=');
        sb.append(((this.source == null)?"<null>":this.source));
        sb.append(',');
        sb.append("metadata");
        sb.append('=');
        sb.append(((this.metadata == null)?"<null>":this.metadata));
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
        result = ((result* 31)+((this.source == null)? 0 :this.source.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof AlternativeTitleType) == false) {
            return false;
        }
        AlternativeTitleType rhs = ((AlternativeTitleType) other);
        return (((((this.name == rhs.name)||((this.name!= null)&&this.name.equals(rhs.name)))&&((this.metadata == rhs.metadata)||((this.metadata!= null)&&this.metadata.equals(rhs.metadata))))&&((this.id == rhs.id)||((this.id!= null)&&this.id.equals(rhs.id))))&&((this.source == rhs.source)||((this.source!= null)&&this.source.equals(rhs.source))));
    }

}
