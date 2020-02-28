
package org.folio.rest.migration.model.generated.source_record_manager;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


/**
 * Raw record DTO schema
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "record",
    "order"
})
public class RawRecordDto {

    /**
     * Raw record content
     * (Required)
     * 
     */
    @JsonProperty("record")
    @JsonPropertyDescription("Raw record content")
    private String record;
    /**
     * Order of the record in incoming file
     * 
     */
    @JsonProperty("order")
    @JsonPropertyDescription("Order of the record in incoming file")
    private Integer order;

    /**
     * Raw record content
     * (Required)
     * 
     */
    @JsonProperty("record")
    public String getRecord() {
        return record;
    }

    /**
     * Raw record content
     * (Required)
     * 
     */
    @JsonProperty("record")
    public void setRecord(String record) {
        this.record = record;
    }

    /**
     * Order of the record in incoming file
     * 
     */
    @JsonProperty("order")
    public Integer getOrder() {
        return order;
    }

    /**
     * Order of the record in incoming file
     * 
     */
    @JsonProperty("order")
    public void setOrder(Integer order) {
        this.order = order;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(RawRecordDto.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("record");
        sb.append('=');
        sb.append(((this.record == null)?"<null>":this.record));
        sb.append(',');
        sb.append("order");
        sb.append('=');
        sb.append(((this.order == null)?"<null>":this.order));
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
        result = ((result* 31)+((this.record == null)? 0 :this.record.hashCode()));
        result = ((result* 31)+((this.order == null)? 0 :this.order.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof RawRecordDto) == false) {
            return false;
        }
        RawRecordDto rhs = ((RawRecordDto) other);
        return (((this.record == rhs.record)||((this.record!= null)&&this.record.equals(rhs.record)))&&((this.order == rhs.order)||((this.order!= null)&&this.order.equals(rhs.order))));
    }

}
