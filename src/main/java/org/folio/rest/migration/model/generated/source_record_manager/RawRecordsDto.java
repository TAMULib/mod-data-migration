
package org.folio.rest.migration.model.generated.source_record_manager;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


/**
 * Schema that describes payload for receiving a chunk of raw records
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "initialRecords",
    "recordsMetadata"
})
public class RawRecordsDto {

    /**
     * List of raw records
     * (Required)
     * 
     */
    @JsonProperty("initialRecords")
    @JsonPropertyDescription("List of raw records")
    private List<RawRecordDto> initialRecords = new ArrayList<RawRecordDto>();
    /**
     * Schema that describes metadata for receiving a chunk of raw records
     * (Required)
     * 
     */
    @JsonProperty("recordsMetadata")
    @JsonPropertyDescription("Schema that describes metadata for receiving a chunk of raw records")
    private RawRecordsMetadata recordsMetadata;

    /**
     * List of raw records
     * (Required)
     * 
     */
    @JsonProperty("initialRecords")
    public List<RawRecordDto> getInitialRecords() {
        return initialRecords;
    }

    /**
     * List of raw records
     * (Required)
     * 
     */
    @JsonProperty("initialRecords")
    public void setInitialRecords(List<RawRecordDto> initialRecords) {
        this.initialRecords = initialRecords;
    }

    /**
     * Schema that describes metadata for receiving a chunk of raw records
     * (Required)
     * 
     */
    @JsonProperty("recordsMetadata")
    public RawRecordsMetadata getRecordsMetadata() {
        return recordsMetadata;
    }

    /**
     * Schema that describes metadata for receiving a chunk of raw records
     * (Required)
     * 
     */
    @JsonProperty("recordsMetadata")
    public void setRecordsMetadata(RawRecordsMetadata recordsMetadata) {
        this.recordsMetadata = recordsMetadata;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(RawRecordsDto.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("initialRecords");
        sb.append('=');
        sb.append(((this.initialRecords == null)?"<null>":this.initialRecords));
        sb.append(',');
        sb.append("recordsMetadata");
        sb.append('=');
        sb.append(((this.recordsMetadata == null)?"<null>":this.recordsMetadata));
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
        result = ((result* 31)+((this.initialRecords == null)? 0 :this.initialRecords.hashCode()));
        result = ((result* 31)+((this.recordsMetadata == null)? 0 :this.recordsMetadata.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof RawRecordsDto) == false) {
            return false;
        }
        RawRecordsDto rhs = ((RawRecordsDto) other);
        return (((this.initialRecords == rhs.initialRecords)||((this.initialRecords!= null)&&this.initialRecords.equals(rhs.initialRecords)))&&((this.recordsMetadata == rhs.recordsMetadata)||((this.recordsMetadata!= null)&&this.recordsMetadata.equals(rhs.recordsMetadata))));
    }

}
