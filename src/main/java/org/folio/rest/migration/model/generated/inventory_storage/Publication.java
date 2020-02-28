
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
    "publisher",
    "place",
    "dateOfPublication",
    "role"
})
public class Publication {

    /**
     * Name of publisher, distributor, etc.
     * 
     */
    @JsonProperty("publisher")
    @JsonPropertyDescription("Name of publisher, distributor, etc.")
    private String publisher;
    /**
     * Place of publication, distribution, etc.
     * 
     */
    @JsonProperty("place")
    @JsonPropertyDescription("Place of publication, distribution, etc.")
    private String place;
    /**
     * Date (year YYYY) of publication, distribution, etc.
     * 
     */
    @JsonProperty("dateOfPublication")
    @JsonPropertyDescription("Date (year YYYY) of publication, distribution, etc.")
    private String dateOfPublication;
    /**
     * The role of the publisher, distributor, etc.
     * 
     */
    @JsonProperty("role")
    @JsonPropertyDescription("The role of the publisher, distributor, etc.")
    private String role;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * Name of publisher, distributor, etc.
     * 
     */
    @JsonProperty("publisher")
    public String getPublisher() {
        return publisher;
    }

    /**
     * Name of publisher, distributor, etc.
     * 
     */
    @JsonProperty("publisher")
    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    /**
     * Place of publication, distribution, etc.
     * 
     */
    @JsonProperty("place")
    public String getPlace() {
        return place;
    }

    /**
     * Place of publication, distribution, etc.
     * 
     */
    @JsonProperty("place")
    public void setPlace(String place) {
        this.place = place;
    }

    /**
     * Date (year YYYY) of publication, distribution, etc.
     * 
     */
    @JsonProperty("dateOfPublication")
    public String getDateOfPublication() {
        return dateOfPublication;
    }

    /**
     * Date (year YYYY) of publication, distribution, etc.
     * 
     */
    @JsonProperty("dateOfPublication")
    public void setDateOfPublication(String dateOfPublication) {
        this.dateOfPublication = dateOfPublication;
    }

    /**
     * The role of the publisher, distributor, etc.
     * 
     */
    @JsonProperty("role")
    public String getRole() {
        return role;
    }

    /**
     * The role of the publisher, distributor, etc.
     * 
     */
    @JsonProperty("role")
    public void setRole(String role) {
        this.role = role;
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
        sb.append(Publication.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("publisher");
        sb.append('=');
        sb.append(((this.publisher == null)?"<null>":this.publisher));
        sb.append(',');
        sb.append("place");
        sb.append('=');
        sb.append(((this.place == null)?"<null>":this.place));
        sb.append(',');
        sb.append("dateOfPublication");
        sb.append('=');
        sb.append(((this.dateOfPublication == null)?"<null>":this.dateOfPublication));
        sb.append(',');
        sb.append("role");
        sb.append('=');
        sb.append(((this.role == null)?"<null>":this.role));
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
        result = ((result* 31)+((this.publisher == null)? 0 :this.publisher.hashCode()));
        result = ((result* 31)+((this.place == null)? 0 :this.place.hashCode()));
        result = ((result* 31)+((this.role == null)? 0 :this.role.hashCode()));
        result = ((result* 31)+((this.additionalProperties == null)? 0 :this.additionalProperties.hashCode()));
        result = ((result* 31)+((this.dateOfPublication == null)? 0 :this.dateOfPublication.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Publication) == false) {
            return false;
        }
        Publication rhs = ((Publication) other);
        return ((((((this.publisher == rhs.publisher)||((this.publisher!= null)&&this.publisher.equals(rhs.publisher)))&&((this.place == rhs.place)||((this.place!= null)&&this.place.equals(rhs.place))))&&((this.role == rhs.role)||((this.role!= null)&&this.role.equals(rhs.role))))&&((this.additionalProperties == rhs.additionalProperties)||((this.additionalProperties!= null)&&this.additionalProperties.equals(rhs.additionalProperties))))&&((this.dateOfPublication == rhs.dateOfPublication)||((this.dateOfPublication!= null)&&this.dateOfPublication.equals(rhs.dateOfPublication))));
    }

}
