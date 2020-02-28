
package org.folio.rest.migration.model.generated.source_record_manager;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;


/**
 * Schema that describes metadata for receiving a chunk of raw records
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "last",
    "counter",
    "total",
    "contentType"
})
public class RawRecordsMetadata {

    /**
     * Indicates if the current chunk is the last one associated with the same JobExecution
     * (Required)
     * 
     */
    @JsonProperty("last")
    @JsonPropertyDescription("Indicates if the current chunk is the last one associated with the same JobExecution")
    private Boolean last;
    /**
     * Counter of records associated with the same JobExecution, the last counter number is a total number of records
     * (Required)
     * 
     */
    @JsonProperty("counter")
    @JsonPropertyDescription("Counter of records associated with the same JobExecution, the last counter number is a total number of records")
    private Integer counter;
    /**
     * Total number of records associated with the same JobExecution
     * 
     */
    @JsonProperty("total")
    @JsonPropertyDescription("Total number of records associated with the same JobExecution")
    private Integer total;
    /**
     * Describes type of records and format of record representation
     * (Required)
     * 
     */
    @JsonProperty("contentType")
    @JsonPropertyDescription("Describes type of records and format of record representation")
    private RawRecordsMetadata.ContentType contentType;

    /**
     * Indicates if the current chunk is the last one associated with the same JobExecution
     * (Required)
     * 
     */
    @JsonProperty("last")
    public Boolean getLast() {
        return last;
    }

    /**
     * Indicates if the current chunk is the last one associated with the same JobExecution
     * (Required)
     * 
     */
    @JsonProperty("last")
    public void setLast(Boolean last) {
        this.last = last;
    }

    /**
     * Counter of records associated with the same JobExecution, the last counter number is a total number of records
     * (Required)
     * 
     */
    @JsonProperty("counter")
    public Integer getCounter() {
        return counter;
    }

    /**
     * Counter of records associated with the same JobExecution, the last counter number is a total number of records
     * (Required)
     * 
     */
    @JsonProperty("counter")
    public void setCounter(Integer counter) {
        this.counter = counter;
    }

    /**
     * Total number of records associated with the same JobExecution
     * 
     */
    @JsonProperty("total")
    public Integer getTotal() {
        return total;
    }

    /**
     * Total number of records associated with the same JobExecution
     * 
     */
    @JsonProperty("total")
    public void setTotal(Integer total) {
        this.total = total;
    }

    /**
     * Describes type of records and format of record representation
     * (Required)
     * 
     */
    @JsonProperty("contentType")
    public RawRecordsMetadata.ContentType getContentType() {
        return contentType;
    }

    /**
     * Describes type of records and format of record representation
     * (Required)
     * 
     */
    @JsonProperty("contentType")
    public void setContentType(RawRecordsMetadata.ContentType contentType) {
        this.contentType = contentType;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(RawRecordsMetadata.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("last");
        sb.append('=');
        sb.append(((this.last == null)?"<null>":this.last));
        sb.append(',');
        sb.append("counter");
        sb.append('=');
        sb.append(((this.counter == null)?"<null>":this.counter));
        sb.append(',');
        sb.append("total");
        sb.append('=');
        sb.append(((this.total == null)?"<null>":this.total));
        sb.append(',');
        sb.append("contentType");
        sb.append('=');
        sb.append(((this.contentType == null)?"<null>":this.contentType));
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
        result = ((result* 31)+((this.total == null)? 0 :this.total.hashCode()));
        result = ((result* 31)+((this.counter == null)? 0 :this.counter.hashCode()));
        result = ((result* 31)+((this.last == null)? 0 :this.last.hashCode()));
        result = ((result* 31)+((this.contentType == null)? 0 :this.contentType.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof RawRecordsMetadata) == false) {
            return false;
        }
        RawRecordsMetadata rhs = ((RawRecordsMetadata) other);
        return (((((this.total == rhs.total)||((this.total!= null)&&this.total.equals(rhs.total)))&&((this.counter == rhs.counter)||((this.counter!= null)&&this.counter.equals(rhs.counter))))&&((this.last == rhs.last)||((this.last!= null)&&this.last.equals(rhs.last))))&&((this.contentType == rhs.contentType)||((this.contentType!= null)&&this.contentType.equals(rhs.contentType))));
    }


    /**
     * Describes type of records and format of record representation
     * 
     */
    public enum ContentType {

        MARC_RAW("MARC_RAW"),
        MARC_JSON("MARC_JSON"),
        MARC_XML("MARC_XML");
        private final String value;
        private final static Map<String, RawRecordsMetadata.ContentType> CONSTANTS = new HashMap<String, RawRecordsMetadata.ContentType>();

        static {
            for (RawRecordsMetadata.ContentType c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private ContentType(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }

        @JsonValue
        public String value() {
            return this.value;
        }

        @JsonCreator
        public static RawRecordsMetadata.ContentType fromValue(String value) {
            RawRecordsMetadata.ContentType constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
