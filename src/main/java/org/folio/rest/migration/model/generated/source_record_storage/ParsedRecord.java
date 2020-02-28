
package org.folio.rest.migration.model.generated.source_record_storage;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


/**
 * Parsed Record Schema
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "content",
    "formattedContent"
})
public class ParsedRecord {

    /**
     * Regexp pattern for UUID validation
     * 
     */
    @JsonProperty("id")
    @JsonPropertyDescription("Regexp pattern for UUID validation")
    private String id;
    /**
     * Parsed record content, e.g. MARC record
     * (Required)
     * 
     */
    @JsonProperty("content")
    @JsonPropertyDescription("Parsed record content, e.g. MARC record")
    private Object content;
    /**
     * Parsed content represented in human readable form
     * 
     */
    @JsonProperty("formattedContent")
    @JsonPropertyDescription("Parsed content represented in human readable form")
    private String formattedContent;

    /**
     * Regexp pattern for UUID validation
     * 
     */
    @JsonProperty("id")
    public String getId() {
        return id;
    }

    /**
     * Regexp pattern for UUID validation
     * 
     */
    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Parsed record content, e.g. MARC record
     * (Required)
     * 
     */
    @JsonProperty("content")
    public Object getContent() {
        return content;
    }

    /**
     * Parsed record content, e.g. MARC record
     * (Required)
     * 
     */
    @JsonProperty("content")
    public void setContent(Object content) {
        this.content = content;
    }

    /**
     * Parsed content represented in human readable form
     * 
     */
    @JsonProperty("formattedContent")
    public String getFormattedContent() {
        return formattedContent;
    }

    /**
     * Parsed content represented in human readable form
     * 
     */
    @JsonProperty("formattedContent")
    public void setFormattedContent(String formattedContent) {
        this.formattedContent = formattedContent;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(ParsedRecord.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("id");
        sb.append('=');
        sb.append(((this.id == null)?"<null>":this.id));
        sb.append(',');
        sb.append("content");
        sb.append('=');
        sb.append(((this.content == null)?"<null>":this.content));
        sb.append(',');
        sb.append("formattedContent");
        sb.append('=');
        sb.append(((this.formattedContent == null)?"<null>":this.formattedContent));
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
        result = ((result* 31)+((this.id == null)? 0 :this.id.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ParsedRecord) == false) {
            return false;
        }
        ParsedRecord rhs = ((ParsedRecord) other);
        return ((this.id == rhs.id)||((this.id!= null)&&this.id.equals(rhs.id)));
    }

}
