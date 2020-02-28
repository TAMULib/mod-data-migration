
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
    "instanceNoteTypeId",
    "note",
    "staffOnly"
})
public class Note {

    /**
     * Regexp pattern for UUID validation
     * 
     */
    @JsonProperty("instanceNoteTypeId")
    @JsonPropertyDescription("Regexp pattern for UUID validation")
    private String instanceNoteTypeId;
    /**
     * Text content of the note
     * 
     */
    @JsonProperty("note")
    @JsonPropertyDescription("Text content of the note")
    private String note;
    /**
     * If true, determines that the note should not be visible for others than staff
     * 
     */
    @JsonProperty("staffOnly")
    @JsonPropertyDescription("If true, determines that the note should not be visible for others than staff")
    private Boolean staffOnly = false;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * Regexp pattern for UUID validation
     * 
     */
    @JsonProperty("instanceNoteTypeId")
    public String getInstanceNoteTypeId() {
        return instanceNoteTypeId;
    }

    /**
     * Regexp pattern for UUID validation
     * 
     */
    @JsonProperty("instanceNoteTypeId")
    public void setInstanceNoteTypeId(String instanceNoteTypeId) {
        this.instanceNoteTypeId = instanceNoteTypeId;
    }

    /**
     * Text content of the note
     * 
     */
    @JsonProperty("note")
    public String getNote() {
        return note;
    }

    /**
     * Text content of the note
     * 
     */
    @JsonProperty("note")
    public void setNote(String note) {
        this.note = note;
    }

    /**
     * If true, determines that the note should not be visible for others than staff
     * 
     */
    @JsonProperty("staffOnly")
    public Boolean getStaffOnly() {
        return staffOnly;
    }

    /**
     * If true, determines that the note should not be visible for others than staff
     * 
     */
    @JsonProperty("staffOnly")
    public void setStaffOnly(Boolean staffOnly) {
        this.staffOnly = staffOnly;
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
        sb.append(Note.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("instanceNoteTypeId");
        sb.append('=');
        sb.append(((this.instanceNoteTypeId == null)?"<null>":this.instanceNoteTypeId));
        sb.append(',');
        sb.append("note");
        sb.append('=');
        sb.append(((this.note == null)?"<null>":this.note));
        sb.append(',');
        sb.append("staffOnly");
        sb.append('=');
        sb.append(((this.staffOnly == null)?"<null>":this.staffOnly));
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
        result = ((result* 31)+((this.note == null)? 0 :this.note.hashCode()));
        result = ((result* 31)+((this.additionalProperties == null)? 0 :this.additionalProperties.hashCode()));
        result = ((result* 31)+((this.instanceNoteTypeId == null)? 0 :this.instanceNoteTypeId.hashCode()));
        result = ((result* 31)+((this.staffOnly == null)? 0 :this.staffOnly.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Note) == false) {
            return false;
        }
        Note rhs = ((Note) other);
        return (((((this.note == rhs.note)||((this.note!= null)&&this.note.equals(rhs.note)))&&((this.additionalProperties == rhs.additionalProperties)||((this.additionalProperties!= null)&&this.additionalProperties.equals(rhs.additionalProperties))))&&((this.instanceNoteTypeId == rhs.instanceNoteTypeId)||((this.instanceNoteTypeId!= null)&&this.instanceNoteTypeId.equals(rhs.instanceNoteTypeId))))&&((this.staffOnly == rhs.staffOnly)||((this.staffOnly!= null)&&this.staffOnly.equals(rhs.staffOnly))));
    }

}
