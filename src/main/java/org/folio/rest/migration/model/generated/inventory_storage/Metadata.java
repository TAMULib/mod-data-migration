
package org.folio.rest.migration.model.generated.inventory_storage;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


/**
 * Metadata Schema
 * <p>
 * Metadata about creation and changes to records, provided by the server (client should not provide)
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "createdDate",
    "createdByUserId",
    "createdByUsername",
    "updatedDate",
    "updatedByUserId",
    "updatedByUsername"
})
public class Metadata {

    /**
     * Date and time when the record was created
     * (Required)
     * 
     */
    @JsonProperty("createdDate")
    @JsonPropertyDescription("Date and time when the record was created")
    private Date createdDate;
    /**
     * ID of the user who created the record
     * (Required)
     * 
     */
    @JsonProperty("createdByUserId")
    @JsonPropertyDescription("ID of the user who created the record")
    private String createdByUserId;
    /**
     * Username of the user who created the record (when available)
     * 
     */
    @JsonProperty("createdByUsername")
    @JsonPropertyDescription("Username of the user who created the record (when available)")
    private String createdByUsername;
    /**
     * Date and time when the record was last updated
     * 
     */
    @JsonProperty("updatedDate")
    @JsonPropertyDescription("Date and time when the record was last updated")
    private Date updatedDate;
    /**
     * ID of the user who last updated the record
     * 
     */
    @JsonProperty("updatedByUserId")
    @JsonPropertyDescription("ID of the user who last updated the record")
    private String updatedByUserId;
    /**
     * Username of the user who last updated the record (when available)
     * 
     */
    @JsonProperty("updatedByUsername")
    @JsonPropertyDescription("Username of the user who last updated the record (when available)")
    private String updatedByUsername;

    /**
     * Date and time when the record was created
     * (Required)
     * 
     */
    @JsonProperty("createdDate")
    public Date getCreatedDate() {
        return createdDate;
    }

    /**
     * Date and time when the record was created
     * (Required)
     * 
     */
    @JsonProperty("createdDate")
    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    /**
     * ID of the user who created the record
     * (Required)
     * 
     */
    @JsonProperty("createdByUserId")
    public String getCreatedByUserId() {
        return createdByUserId;
    }

    /**
     * ID of the user who created the record
     * (Required)
     * 
     */
    @JsonProperty("createdByUserId")
    public void setCreatedByUserId(String createdByUserId) {
        this.createdByUserId = createdByUserId;
    }

    /**
     * Username of the user who created the record (when available)
     * 
     */
    @JsonProperty("createdByUsername")
    public String getCreatedByUsername() {
        return createdByUsername;
    }

    /**
     * Username of the user who created the record (when available)
     * 
     */
    @JsonProperty("createdByUsername")
    public void setCreatedByUsername(String createdByUsername) {
        this.createdByUsername = createdByUsername;
    }

    /**
     * Date and time when the record was last updated
     * 
     */
    @JsonProperty("updatedDate")
    public Date getUpdatedDate() {
        return updatedDate;
    }

    /**
     * Date and time when the record was last updated
     * 
     */
    @JsonProperty("updatedDate")
    public void setUpdatedDate(Date updatedDate) {
        this.updatedDate = updatedDate;
    }

    /**
     * ID of the user who last updated the record
     * 
     */
    @JsonProperty("updatedByUserId")
    public String getUpdatedByUserId() {
        return updatedByUserId;
    }

    /**
     * ID of the user who last updated the record
     * 
     */
    @JsonProperty("updatedByUserId")
    public void setUpdatedByUserId(String updatedByUserId) {
        this.updatedByUserId = updatedByUserId;
    }

    /**
     * Username of the user who last updated the record (when available)
     * 
     */
    @JsonProperty("updatedByUsername")
    public String getUpdatedByUsername() {
        return updatedByUsername;
    }

    /**
     * Username of the user who last updated the record (when available)
     * 
     */
    @JsonProperty("updatedByUsername")
    public void setUpdatedByUsername(String updatedByUsername) {
        this.updatedByUsername = updatedByUsername;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Metadata.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("createdDate");
        sb.append('=');
        sb.append(((this.createdDate == null)?"<null>":this.createdDate));
        sb.append(',');
        sb.append("createdByUserId");
        sb.append('=');
        sb.append(((this.createdByUserId == null)?"<null>":this.createdByUserId));
        sb.append(',');
        sb.append("createdByUsername");
        sb.append('=');
        sb.append(((this.createdByUsername == null)?"<null>":this.createdByUsername));
        sb.append(',');
        sb.append("updatedDate");
        sb.append('=');
        sb.append(((this.updatedDate == null)?"<null>":this.updatedDate));
        sb.append(',');
        sb.append("updatedByUserId");
        sb.append('=');
        sb.append(((this.updatedByUserId == null)?"<null>":this.updatedByUserId));
        sb.append(',');
        sb.append("updatedByUsername");
        sb.append('=');
        sb.append(((this.updatedByUsername == null)?"<null>":this.updatedByUsername));
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
        result = ((result* 31)+((this.createdByUserId == null)? 0 :this.createdByUserId.hashCode()));
        result = ((result* 31)+((this.updatedByUsername == null)? 0 :this.updatedByUsername.hashCode()));
        result = ((result* 31)+((this.createdDate == null)? 0 :this.createdDate.hashCode()));
        result = ((result* 31)+((this.createdByUsername == null)? 0 :this.createdByUsername.hashCode()));
        result = ((result* 31)+((this.updatedDate == null)? 0 :this.updatedDate.hashCode()));
        result = ((result* 31)+((this.updatedByUserId == null)? 0 :this.updatedByUserId.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Metadata) == false) {
            return false;
        }
        Metadata rhs = ((Metadata) other);
        return (((((((this.createdByUserId == rhs.createdByUserId)||((this.createdByUserId!= null)&&this.createdByUserId.equals(rhs.createdByUserId)))&&((this.updatedByUsername == rhs.updatedByUsername)||((this.updatedByUsername!= null)&&this.updatedByUsername.equals(rhs.updatedByUsername))))&&((this.createdDate == rhs.createdDate)||((this.createdDate!= null)&&this.createdDate.equals(rhs.createdDate))))&&((this.createdByUsername == rhs.createdByUsername)||((this.createdByUsername!= null)&&this.createdByUsername.equals(rhs.createdByUsername))))&&((this.updatedDate == rhs.updatedDate)||((this.updatedDate!= null)&&this.updatedDate.equals(rhs.updatedDate))))&&((this.updatedByUserId == rhs.updatedByUserId)||((this.updatedByUserId!= null)&&this.updatedByUserId.equals(rhs.updatedByUserId))));
    }

}
