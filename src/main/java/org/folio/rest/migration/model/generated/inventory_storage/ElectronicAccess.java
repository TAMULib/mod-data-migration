
package org.folio.rest.migration.model.generated.inventory_storage;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "uri",
    "linkText",
    "materialsSpecification",
    "publicNote",
    "relationshipId"
})
public class ElectronicAccess {

    /**
     * uniform resource identifier (URI) is a string of characters designed for unambiguous identification of resources
     * (Required)
     * 
     */
    @JsonProperty("uri")
    @JsonPropertyDescription("uniform resource identifier (URI) is a string of characters designed for unambiguous identification of resources")
    private String uri;
    /**
     * The value of the MARC tag field 856 2nd indicator, where the values are: no information provided, resource, version of resource, related resource, no display constant generated
     * 
     */
    @JsonProperty("linkText")
    @JsonPropertyDescription("The value of the MARC tag field 856 2nd indicator, where the values are: no information provided, resource, version of resource, related resource, no display constant generated")
    private String linkText;
    /**
     * Materials specified is used to specify to what portion or aspect of the resource the electronic location and access information applies (e.g. a portion or subset of the item is electronic, or a related electronic resource is being linked to the record)
     * 
     */
    @JsonProperty("materialsSpecification")
    @JsonPropertyDescription("Materials specified is used to specify to what portion or aspect of the resource the electronic location and access information applies (e.g. a portion or subset of the item is electronic, or a related electronic resource is being linked to the record)")
    private String materialsSpecification;
    /**
     * URL public note to be displayed in the discovery
     * 
     */
    @JsonProperty("publicNote")
    @JsonPropertyDescription("URL public note to be displayed in the discovery")
    private String publicNote;
    /**
     * Regexp pattern for UUID validation
     * 
     */
    @JsonProperty("relationshipId")
    @JsonPropertyDescription("Regexp pattern for UUID validation")
    private String relationshipId;

    /**
     * uniform resource identifier (URI) is a string of characters designed for unambiguous identification of resources
     * (Required)
     * 
     */
    @JsonProperty("uri")
    public String getUri() {
        return uri;
    }

    /**
     * uniform resource identifier (URI) is a string of characters designed for unambiguous identification of resources
     * (Required)
     * 
     */
    @JsonProperty("uri")
    public void setUri(String uri) {
        this.uri = uri;
    }

    /**
     * The value of the MARC tag field 856 2nd indicator, where the values are: no information provided, resource, version of resource, related resource, no display constant generated
     * 
     */
    @JsonProperty("linkText")
    public String getLinkText() {
        return linkText;
    }

    /**
     * The value of the MARC tag field 856 2nd indicator, where the values are: no information provided, resource, version of resource, related resource, no display constant generated
     * 
     */
    @JsonProperty("linkText")
    public void setLinkText(String linkText) {
        this.linkText = linkText;
    }

    /**
     * Materials specified is used to specify to what portion or aspect of the resource the electronic location and access information applies (e.g. a portion or subset of the item is electronic, or a related electronic resource is being linked to the record)
     * 
     */
    @JsonProperty("materialsSpecification")
    public String getMaterialsSpecification() {
        return materialsSpecification;
    }

    /**
     * Materials specified is used to specify to what portion or aspect of the resource the electronic location and access information applies (e.g. a portion or subset of the item is electronic, or a related electronic resource is being linked to the record)
     * 
     */
    @JsonProperty("materialsSpecification")
    public void setMaterialsSpecification(String materialsSpecification) {
        this.materialsSpecification = materialsSpecification;
    }

    /**
     * URL public note to be displayed in the discovery
     * 
     */
    @JsonProperty("publicNote")
    public String getPublicNote() {
        return publicNote;
    }

    /**
     * URL public note to be displayed in the discovery
     * 
     */
    @JsonProperty("publicNote")
    public void setPublicNote(String publicNote) {
        this.publicNote = publicNote;
    }

    /**
     * Regexp pattern for UUID validation
     * 
     */
    @JsonProperty("relationshipId")
    public String getRelationshipId() {
        return relationshipId;
    }

    /**
     * Regexp pattern for UUID validation
     * 
     */
    @JsonProperty("relationshipId")
    public void setRelationshipId(String relationshipId) {
        this.relationshipId = relationshipId;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(ElectronicAccess.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("uri");
        sb.append('=');
        sb.append(((this.uri == null)?"<null>":this.uri));
        sb.append(',');
        sb.append("linkText");
        sb.append('=');
        sb.append(((this.linkText == null)?"<null>":this.linkText));
        sb.append(',');
        sb.append("materialsSpecification");
        sb.append('=');
        sb.append(((this.materialsSpecification == null)?"<null>":this.materialsSpecification));
        sb.append(',');
        sb.append("publicNote");
        sb.append('=');
        sb.append(((this.publicNote == null)?"<null>":this.publicNote));
        sb.append(',');
        sb.append("relationshipId");
        sb.append('=');
        sb.append(((this.relationshipId == null)?"<null>":this.relationshipId));
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
        result = ((result* 31)+((this.materialsSpecification == null)? 0 :this.materialsSpecification.hashCode()));
        result = ((result* 31)+((this.linkText == null)? 0 :this.linkText.hashCode()));
        result = ((result* 31)+((this.publicNote == null)? 0 :this.publicNote.hashCode()));
        result = ((result* 31)+((this.relationshipId == null)? 0 :this.relationshipId.hashCode()));
        result = ((result* 31)+((this.uri == null)? 0 :this.uri.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ElectronicAccess) == false) {
            return false;
        }
        ElectronicAccess rhs = ((ElectronicAccess) other);
        return ((((((this.materialsSpecification == rhs.materialsSpecification)||((this.materialsSpecification!= null)&&this.materialsSpecification.equals(rhs.materialsSpecification)))&&((this.linkText == rhs.linkText)||((this.linkText!= null)&&this.linkText.equals(rhs.linkText))))&&((this.publicNote == rhs.publicNote)||((this.publicNote!= null)&&this.publicNote.equals(rhs.publicNote))))&&((this.relationshipId == rhs.relationshipId)||((this.relationshipId!= null)&&this.relationshipId.equals(rhs.relationshipId))))&&((this.uri == rhs.uri)||((this.uri!= null)&&this.uri.equals(rhs.uri))));
    }

}
