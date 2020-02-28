
package org.folio.rest.migration.model.generated.source_record_manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;
import org.folio.rest.migration.model.generated.common.ProfileInfo;


/**
 * Request to initialize JobExecution entities
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "files",
    "sourceType",
    "jobProfileInfo",
    "userId"
})
public class InitJobExecutionsRqDto {

    /**
     * Information about files to upload
     * 
     */
    @JsonProperty("files")
    @JsonPropertyDescription("Information about files to upload")
    private List<FileDto> files = new ArrayList<FileDto>();
    /**
     * Raw records source type Enum
     * (Required)
     * 
     */
    @JsonProperty("sourceType")
    @JsonPropertyDescription("Raw records source type Enum")
    private InitJobExecutionsRqDto.SourceType sourceType;
    /**
     * Related JobProfile information
     * 
     */
    @JsonProperty("jobProfileInfo")
    @JsonPropertyDescription("Related JobProfile information")
    private ProfileInfo jobProfileInfo;
    /**
     * Regexp pattern for UUID validation
     * (Required)
     * 
     */
    @JsonProperty("userId")
    @JsonPropertyDescription("Regexp pattern for UUID validation")
    private String userId;

    /**
     * Information about files to upload
     * 
     */
    @JsonProperty("files")
    public List<FileDto> getFiles() {
        return files;
    }

    /**
     * Information about files to upload
     * 
     */
    @JsonProperty("files")
    public void setFiles(List<FileDto> files) {
        this.files = files;
    }

    /**
     * Raw records source type Enum
     * (Required)
     * 
     */
    @JsonProperty("sourceType")
    public InitJobExecutionsRqDto.SourceType getSourceType() {
        return sourceType;
    }

    /**
     * Raw records source type Enum
     * (Required)
     * 
     */
    @JsonProperty("sourceType")
    public void setSourceType(InitJobExecutionsRqDto.SourceType sourceType) {
        this.sourceType = sourceType;
    }

    /**
     * Related JobProfile information
     * 
     */
    @JsonProperty("jobProfileInfo")
    public ProfileInfo getJobProfileInfo() {
        return jobProfileInfo;
    }

    /**
     * Related JobProfile information
     * 
     */
    @JsonProperty("jobProfileInfo")
    public void setJobProfileInfo(ProfileInfo jobProfileInfo) {
        this.jobProfileInfo = jobProfileInfo;
    }

    /**
     * Regexp pattern for UUID validation
     * (Required)
     * 
     */
    @JsonProperty("userId")
    public String getUserId() {
        return userId;
    }

    /**
     * Regexp pattern for UUID validation
     * (Required)
     * 
     */
    @JsonProperty("userId")
    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(InitJobExecutionsRqDto.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("files");
        sb.append('=');
        sb.append(((this.files == null)?"<null>":this.files));
        sb.append(',');
        sb.append("sourceType");
        sb.append('=');
        sb.append(((this.sourceType == null)?"<null>":this.sourceType));
        sb.append(',');
        sb.append("jobProfileInfo");
        sb.append('=');
        sb.append(((this.jobProfileInfo == null)?"<null>":this.jobProfileInfo));
        sb.append(',');
        sb.append("userId");
        sb.append('=');
        sb.append(((this.userId == null)?"<null>":this.userId));
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
        result = ((result* 31)+((this.jobProfileInfo == null)? 0 :this.jobProfileInfo.hashCode()));
        result = ((result* 31)+((this.files == null)? 0 :this.files.hashCode()));
        result = ((result* 31)+((this.sourceType == null)? 0 :this.sourceType.hashCode()));
        result = ((result* 31)+((this.userId == null)? 0 :this.userId.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof InitJobExecutionsRqDto) == false) {
            return false;
        }
        InitJobExecutionsRqDto rhs = ((InitJobExecutionsRqDto) other);
        return (((((this.jobProfileInfo == rhs.jobProfileInfo)||((this.jobProfileInfo!= null)&&this.jobProfileInfo.equals(rhs.jobProfileInfo)))&&((this.files == rhs.files)||((this.files!= null)&&this.files.equals(rhs.files))))&&((this.sourceType == rhs.sourceType)||((this.sourceType!= null)&&this.sourceType.equals(rhs.sourceType))))&&((this.userId == rhs.userId)||((this.userId!= null)&&this.userId.equals(rhs.userId))));
    }


    /**
     * Raw records source type Enum
     * 
     */
    public enum SourceType {

        FILES("FILES"),
        ONLINE("ONLINE");
        private final String value;
        private final static Map<String, InitJobExecutionsRqDto.SourceType> CONSTANTS = new HashMap<String, InitJobExecutionsRqDto.SourceType>();

        static {
            for (InitJobExecutionsRqDto.SourceType c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private SourceType(String value) {
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
        public static InitJobExecutionsRqDto.SourceType fromValue(String value) {
            InitJobExecutionsRqDto.SourceType constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
