
package org.folio.rest.migration.model.generated.inventory_storage;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "classificationNumber",
    "classificationTypeId"
})
public class Classification {

    /**
     * Classification (e.g. classification scheme, classification schedule)
     * (Required)
     * 
     */
    @JsonProperty("classificationNumber")
    @JsonPropertyDescription("Classification (e.g. classification scheme, classification schedule)")
    private String classificationNumber;
    /**
     * Regexp pattern for UUID validation
     * (Required)
     * 
     */
    @JsonProperty("classificationTypeId")
    @JsonPropertyDescription("Regexp pattern for UUID validation")
    private String classificationTypeId;

    /**
     * Classification (e.g. classification scheme, classification schedule)
     * (Required)
     * 
     */
    @JsonProperty("classificationNumber")
    public String getClassificationNumber() {
        return classificationNumber;
    }

    /**
     * Classification (e.g. classification scheme, classification schedule)
     * (Required)
     * 
     */
    @JsonProperty("classificationNumber")
    public void setClassificationNumber(String classificationNumber) {
        this.classificationNumber = classificationNumber;
    }

    /**
     * Regexp pattern for UUID validation
     * (Required)
     * 
     */
    @JsonProperty("classificationTypeId")
    public String getClassificationTypeId() {
        return classificationTypeId;
    }

    /**
     * Regexp pattern for UUID validation
     * (Required)
     * 
     */
    @JsonProperty("classificationTypeId")
    public void setClassificationTypeId(String classificationTypeId) {
        this.classificationTypeId = classificationTypeId;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Classification.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("classificationNumber");
        sb.append('=');
        sb.append(((this.classificationNumber == null)?"<null>":this.classificationNumber));
        sb.append(',');
        sb.append("classificationTypeId");
        sb.append('=');
        sb.append(((this.classificationTypeId == null)?"<null>":this.classificationTypeId));
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
        result = ((result* 31)+((this.classificationNumber == null)? 0 :this.classificationNumber.hashCode()));
        result = ((result* 31)+((this.classificationTypeId == null)? 0 :this.classificationTypeId.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Classification) == false) {
            return false;
        }
        Classification rhs = ((Classification) other);
        return (((this.classificationNumber == rhs.classificationNumber)||((this.classificationNumber!= null)&&this.classificationNumber.equals(rhs.classificationNumber)))&&((this.classificationTypeId == rhs.classificationTypeId)||((this.classificationTypeId!= null)&&this.classificationTypeId.equals(rhs.classificationTypeId))));
    }

}
