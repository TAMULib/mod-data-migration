
package org.folio.rest.migration.model.generated.inventory_storage;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "name",
    "contributorTypeId",
    "contributorTypeText",
    "contributorNameTypeId",
    "primary"
})
public class Contributor {

    /**
     * Personal name, corporate name, meeting name
     * (Required)
     * 
     */
    @JsonProperty("name")
    @JsonPropertyDescription("Personal name, corporate name, meeting name")
    private String name;
    /**
     * Regexp pattern for UUID validation
     * 
     */
    @JsonProperty("contributorTypeId")
    @JsonPropertyDescription("Regexp pattern for UUID validation")
    private String contributorTypeId;
    /**
     * Free text element for adding contributor type terms other that defined by the MARC code list for relators
     * 
     */
    @JsonProperty("contributorTypeText")
    @JsonPropertyDescription("Free text element for adding contributor type terms other that defined by the MARC code list for relators")
    private String contributorTypeText;
    /**
     * Regexp pattern for UUID validation
     * (Required)
     * 
     */
    @JsonProperty("contributorNameTypeId")
    @JsonPropertyDescription("Regexp pattern for UUID validation")
    private String contributorNameTypeId;
    /**
     * Whether this is the primary contributor
     * 
     */
    @JsonProperty("primary")
    @JsonPropertyDescription("Whether this is the primary contributor")
    private Boolean primary;

    /**
     * Personal name, corporate name, meeting name
     * (Required)
     * 
     */
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    /**
     * Personal name, corporate name, meeting name
     * (Required)
     * 
     */
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Regexp pattern for UUID validation
     * 
     */
    @JsonProperty("contributorTypeId")
    public String getContributorTypeId() {
        return contributorTypeId;
    }

    /**
     * Regexp pattern for UUID validation
     * 
     */
    @JsonProperty("contributorTypeId")
    public void setContributorTypeId(String contributorTypeId) {
        this.contributorTypeId = contributorTypeId;
    }

    /**
     * Free text element for adding contributor type terms other that defined by the MARC code list for relators
     * 
     */
    @JsonProperty("contributorTypeText")
    public String getContributorTypeText() {
        return contributorTypeText;
    }

    /**
     * Free text element for adding contributor type terms other that defined by the MARC code list for relators
     * 
     */
    @JsonProperty("contributorTypeText")
    public void setContributorTypeText(String contributorTypeText) {
        this.contributorTypeText = contributorTypeText;
    }

    /**
     * Regexp pattern for UUID validation
     * (Required)
     * 
     */
    @JsonProperty("contributorNameTypeId")
    public String getContributorNameTypeId() {
        return contributorNameTypeId;
    }

    /**
     * Regexp pattern for UUID validation
     * (Required)
     * 
     */
    @JsonProperty("contributorNameTypeId")
    public void setContributorNameTypeId(String contributorNameTypeId) {
        this.contributorNameTypeId = contributorNameTypeId;
    }

    /**
     * Whether this is the primary contributor
     * 
     */
    @JsonProperty("primary")
    public Boolean getPrimary() {
        return primary;
    }

    /**
     * Whether this is the primary contributor
     * 
     */
    @JsonProperty("primary")
    public void setPrimary(Boolean primary) {
        this.primary = primary;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Contributor.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("name");
        sb.append('=');
        sb.append(((this.name == null)?"<null>":this.name));
        sb.append(',');
        sb.append("contributorTypeId");
        sb.append('=');
        sb.append(((this.contributorTypeId == null)?"<null>":this.contributorTypeId));
        sb.append(',');
        sb.append("contributorTypeText");
        sb.append('=');
        sb.append(((this.contributorTypeText == null)?"<null>":this.contributorTypeText));
        sb.append(',');
        sb.append("contributorNameTypeId");
        sb.append('=');
        sb.append(((this.contributorNameTypeId == null)?"<null>":this.contributorNameTypeId));
        sb.append(',');
        sb.append("primary");
        sb.append('=');
        sb.append(((this.primary == null)?"<null>":this.primary));
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
        result = ((result* 31)+((this.contributorTypeId == null)? 0 :this.contributorTypeId.hashCode()));
        result = ((result* 31)+((this.contributorNameTypeId == null)? 0 :this.contributorNameTypeId.hashCode()));
        result = ((result* 31)+((this.contributorTypeText == null)? 0 :this.contributorTypeText.hashCode()));
        result = ((result* 31)+((this.primary == null)? 0 :this.primary.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Contributor) == false) {
            return false;
        }
        Contributor rhs = ((Contributor) other);
        return ((((((this.name == rhs.name)||((this.name!= null)&&this.name.equals(rhs.name)))&&((this.contributorTypeId == rhs.contributorTypeId)||((this.contributorTypeId!= null)&&this.contributorTypeId.equals(rhs.contributorTypeId))))&&((this.contributorNameTypeId == rhs.contributorNameTypeId)||((this.contributorNameTypeId!= null)&&this.contributorNameTypeId.equals(rhs.contributorNameTypeId))))&&((this.contributorTypeText == rhs.contributorTypeText)||((this.contributorTypeText!= null)&&this.contributorTypeText.equals(rhs.contributorTypeText))))&&((this.primary == rhs.primary)||((this.primary!= null)&&this.primary.equals(rhs.primary))));
    }

}
