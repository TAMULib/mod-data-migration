
package org.folio.rest.migration.model.generated.source_record_manager;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;


/**
 * JobExecution error status Enum
 * 
 */
public enum ErrorStatus {

    SNAPSHOT_UPDATE_ERROR("SNAPSHOT_UPDATE_ERROR"),
    RECORD_UPDATE_ERROR("RECORD_UPDATE_ERROR"),
    FILE_PROCESSING_ERROR("FILE_PROCESSING_ERROR"),
    INSTANCE_CREATING_ERROR("INSTANCE_CREATING_ERROR");
    private final String value;
    private final static Map<String, ErrorStatus> CONSTANTS = new HashMap<String, ErrorStatus>();

    static {
        for (ErrorStatus c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private ErrorStatus(String value) {
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
    public static ErrorStatus fromValue(String value) {
        ErrorStatus constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
