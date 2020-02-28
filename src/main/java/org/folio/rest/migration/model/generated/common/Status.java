
package org.folio.rest.migration.model.generated.common;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;


/**
 * JobExecution status Enum
 * 
 */
public enum Status {

    PARENT("PARENT"),
    NEW("NEW"),
    FILE_UPLOADED("FILE_UPLOADED"),
    PARSING_IN_PROGRESS("PARSING_IN_PROGRESS"),
    PARSING_FINISHED("PARSING_FINISHED"),
    PROCESSING_IN_PROGRESS("PROCESSING_IN_PROGRESS"),
    PROCESSING_FINISHED("PROCESSING_FINISHED"),
    COMMIT_IN_PROGRESS("COMMIT_IN_PROGRESS"),
    COMMITTED("COMMITTED"),
    ERROR("ERROR"),
    DISCARDED("DISCARDED");
    private final String value;
    private final static Map<String, Status> CONSTANTS = new HashMap<String, Status>();

    static {
        for (Status c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private Status(String value) {
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
    public static Status fromValue(String value) {
        Status constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
