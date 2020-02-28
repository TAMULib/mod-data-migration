
package org.folio.rest.migration.model.generated.source_record_manager;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;
import org.folio.rest.migration.model.generated.common.ProfileInfo;
import org.folio.rest.migration.model.generated.common.Status;


/**
 * Job Execution Schema
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "hrId",
    "parentJobId",
    "subordinationType",
    "jobProfileInfo",
    "sourcePath",
    "fileName",
    "runBy",
    "progress",
    "startedDate",
    "completedDate",
    "status",
    "uiStatus",
    "errorStatus",
    "userId"
})
public class JobExecution {

    /**
     * Regexp pattern for UUID validation
     * (Required)
     * 
     */
    @JsonProperty("id")
    @JsonPropertyDescription("Regexp pattern for UUID validation")
    private String id;
    /**
     * Human readable id
     * 
     */
    @JsonProperty("hrId")
    @JsonPropertyDescription("Human readable id")
    private Integer hrId;
    /**
     * Regexp pattern for UUID validation
     * (Required)
     * 
     */
    @JsonProperty("parentJobId")
    @JsonPropertyDescription("Regexp pattern for UUID validation")
    private String parentJobId;
    /**
     * Type of subordination to another JobExecution entities
     * (Required)
     * 
     */
    @JsonProperty("subordinationType")
    @JsonPropertyDescription("Type of subordination to another JobExecution entities")
    private JobExecution.SubordinationType subordinationType;
    /**
     * Related JobProfile information
     * 
     */
    @JsonProperty("jobProfileInfo")
    @JsonPropertyDescription("Related JobProfile information")
    private ProfileInfo jobProfileInfo;
    /**
     * Path to the file
     * 
     */
    @JsonProperty("sourcePath")
    @JsonPropertyDescription("Path to the file")
    private String sourcePath;
    /**
     * File name
     * 
     */
    @JsonProperty("fileName")
    @JsonPropertyDescription("File name")
    private String fileName;
    /**
     * First and last name of the user that triggered the job execution
     * 
     */
    @JsonProperty("runBy")
    @JsonPropertyDescription("First and last name of the user that triggered the job execution")
    private RunBy runBy;
    /**
     * Execution progress of the job
     * 
     */
    @JsonProperty("progress")
    @JsonPropertyDescription("Execution progress of the job")
    private Progress progress;
    /**
     * Date and time when the job execution started
     * 
     */
    @JsonProperty("startedDate")
    @JsonPropertyDescription("Date and time when the job execution started")
    private Date startedDate;
    /**
     * Date and time when the job execution completed
     * 
     */
    @JsonProperty("completedDate")
    @JsonPropertyDescription("Date and time when the job execution completed")
    private Date completedDate;
    /**
     * JobExecution status Enum
     * (Required)
     * 
     */
    @JsonProperty("status")
    @JsonPropertyDescription("JobExecution status Enum")
    private Status status;
    /**
     * JobExecution UI status Enum
     * (Required)
     * 
     */
    @JsonProperty("uiStatus")
    @JsonPropertyDescription("JobExecution UI status Enum")
    private JobExecution.UiStatus uiStatus;
    /**
     * JobExecution error status Enum
     * 
     */
    @JsonProperty("errorStatus")
    @JsonPropertyDescription("JobExecution error status Enum")
    private ErrorStatus errorStatus;
    /**
     * Regexp pattern for UUID validation
     * (Required)
     * 
     */
    @JsonProperty("userId")
    @JsonPropertyDescription("Regexp pattern for UUID validation")
    private String userId;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * Regexp pattern for UUID validation
     * (Required)
     * 
     */
    @JsonProperty("id")
    public String getId() {
        return id;
    }

    /**
     * Regexp pattern for UUID validation
     * (Required)
     * 
     */
    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Human readable id
     * 
     */
    @JsonProperty("hrId")
    public Integer getHrId() {
        return hrId;
    }

    /**
     * Human readable id
     * 
     */
    @JsonProperty("hrId")
    public void setHrId(Integer hrId) {
        this.hrId = hrId;
    }

    /**
     * Regexp pattern for UUID validation
     * (Required)
     * 
     */
    @JsonProperty("parentJobId")
    public String getParentJobId() {
        return parentJobId;
    }

    /**
     * Regexp pattern for UUID validation
     * (Required)
     * 
     */
    @JsonProperty("parentJobId")
    public void setParentJobId(String parentJobId) {
        this.parentJobId = parentJobId;
    }

    /**
     * Type of subordination to another JobExecution entities
     * (Required)
     * 
     */
    @JsonProperty("subordinationType")
    public JobExecution.SubordinationType getSubordinationType() {
        return subordinationType;
    }

    /**
     * Type of subordination to another JobExecution entities
     * (Required)
     * 
     */
    @JsonProperty("subordinationType")
    public void setSubordinationType(JobExecution.SubordinationType subordinationType) {
        this.subordinationType = subordinationType;
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
     * Path to the file
     * 
     */
    @JsonProperty("sourcePath")
    public String getSourcePath() {
        return sourcePath;
    }

    /**
     * Path to the file
     * 
     */
    @JsonProperty("sourcePath")
    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    /**
     * File name
     * 
     */
    @JsonProperty("fileName")
    public String getFileName() {
        return fileName;
    }

    /**
     * File name
     * 
     */
    @JsonProperty("fileName")
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * First and last name of the user that triggered the job execution
     * 
     */
    @JsonProperty("runBy")
    public RunBy getRunBy() {
        return runBy;
    }

    /**
     * First and last name of the user that triggered the job execution
     * 
     */
    @JsonProperty("runBy")
    public void setRunBy(RunBy runBy) {
        this.runBy = runBy;
    }

    /**
     * Execution progress of the job
     * 
     */
    @JsonProperty("progress")
    public Progress getProgress() {
        return progress;
    }

    /**
     * Execution progress of the job
     * 
     */
    @JsonProperty("progress")
    public void setProgress(Progress progress) {
        this.progress = progress;
    }

    /**
     * Date and time when the job execution started
     * 
     */
    @JsonProperty("startedDate")
    public Date getStartedDate() {
        return startedDate;
    }

    /**
     * Date and time when the job execution started
     * 
     */
    @JsonProperty("startedDate")
    public void setStartedDate(Date startedDate) {
        this.startedDate = startedDate;
    }

    /**
     * Date and time when the job execution completed
     * 
     */
    @JsonProperty("completedDate")
    public Date getCompletedDate() {
        return completedDate;
    }

    /**
     * Date and time when the job execution completed
     * 
     */
    @JsonProperty("completedDate")
    public void setCompletedDate(Date completedDate) {
        this.completedDate = completedDate;
    }

    /**
     * JobExecution status Enum
     * (Required)
     * 
     */
    @JsonProperty("status")
    public Status getStatus() {
        return status;
    }

    /**
     * JobExecution status Enum
     * (Required)
     * 
     */
    @JsonProperty("status")
    public void setStatus(Status status) {
        this.status = status;
    }

    /**
     * JobExecution UI status Enum
     * (Required)
     * 
     */
    @JsonProperty("uiStatus")
    public JobExecution.UiStatus getUiStatus() {
        return uiStatus;
    }

    /**
     * JobExecution UI status Enum
     * (Required)
     * 
     */
    @JsonProperty("uiStatus")
    public void setUiStatus(JobExecution.UiStatus uiStatus) {
        this.uiStatus = uiStatus;
    }

    /**
     * JobExecution error status Enum
     * 
     */
    @JsonProperty("errorStatus")
    public ErrorStatus getErrorStatus() {
        return errorStatus;
    }

    /**
     * JobExecution error status Enum
     * 
     */
    @JsonProperty("errorStatus")
    public void setErrorStatus(ErrorStatus errorStatus) {
        this.errorStatus = errorStatus;
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
        sb.append(JobExecution.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("id");
        sb.append('=');
        sb.append(((this.id == null)?"<null>":this.id));
        sb.append(',');
        sb.append("hrId");
        sb.append('=');
        sb.append(((this.hrId == null)?"<null>":this.hrId));
        sb.append(',');
        sb.append("parentJobId");
        sb.append('=');
        sb.append(((this.parentJobId == null)?"<null>":this.parentJobId));
        sb.append(',');
        sb.append("subordinationType");
        sb.append('=');
        sb.append(((this.subordinationType == null)?"<null>":this.subordinationType));
        sb.append(',');
        sb.append("jobProfileInfo");
        sb.append('=');
        sb.append(((this.jobProfileInfo == null)?"<null>":this.jobProfileInfo));
        sb.append(',');
        sb.append("sourcePath");
        sb.append('=');
        sb.append(((this.sourcePath == null)?"<null>":this.sourcePath));
        sb.append(',');
        sb.append("fileName");
        sb.append('=');
        sb.append(((this.fileName == null)?"<null>":this.fileName));
        sb.append(',');
        sb.append("runBy");
        sb.append('=');
        sb.append(((this.runBy == null)?"<null>":this.runBy));
        sb.append(',');
        sb.append("progress");
        sb.append('=');
        sb.append(((this.progress == null)?"<null>":this.progress));
        sb.append(',');
        sb.append("startedDate");
        sb.append('=');
        sb.append(((this.startedDate == null)?"<null>":this.startedDate));
        sb.append(',');
        sb.append("completedDate");
        sb.append('=');
        sb.append(((this.completedDate == null)?"<null>":this.completedDate));
        sb.append(',');
        sb.append("status");
        sb.append('=');
        sb.append(((this.status == null)?"<null>":this.status));
        sb.append(',');
        sb.append("uiStatus");
        sb.append('=');
        sb.append(((this.uiStatus == null)?"<null>":this.uiStatus));
        sb.append(',');
        sb.append("errorStatus");
        sb.append('=');
        sb.append(((this.errorStatus == null)?"<null>":this.errorStatus));
        sb.append(',');
        sb.append("userId");
        sb.append('=');
        sb.append(((this.userId == null)?"<null>":this.userId));
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
        result = ((result* 31)+((this.fileName == null)? 0 :this.fileName.hashCode()));
        result = ((result* 31)+((this.subordinationType == null)? 0 :this.subordinationType.hashCode()));
        result = ((result* 31)+((this.startedDate == null)? 0 :this.startedDate.hashCode()));
        result = ((result* 31)+((this.uiStatus == null)? 0 :this.uiStatus.hashCode()));
        result = ((result* 31)+((this.userId == null)? 0 :this.userId.hashCode()));
        result = ((result* 31)+((this.completedDate == null)? 0 :this.completedDate.hashCode()));
        result = ((result* 31)+((this.parentJobId == null)? 0 :this.parentJobId.hashCode()));
        result = ((result* 31)+((this.jobProfileInfo == null)? 0 :this.jobProfileInfo.hashCode()));
        result = ((result* 31)+((this.errorStatus == null)? 0 :this.errorStatus.hashCode()));
        result = ((result* 31)+((this.id == null)? 0 :this.id.hashCode()));
        result = ((result* 31)+((this.additionalProperties == null)? 0 :this.additionalProperties.hashCode()));
        result = ((result* 31)+((this.sourcePath == null)? 0 :this.sourcePath.hashCode()));
        result = ((result* 31)+((this.status == null)? 0 :this.status.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof JobExecution) == false) {
            return false;
        }
        JobExecution rhs = ((JobExecution) other);
        return ((((((((((((((this.fileName == rhs.fileName)||((this.fileName!= null)&&this.fileName.equals(rhs.fileName)))&&((this.subordinationType == rhs.subordinationType)||((this.subordinationType!= null)&&this.subordinationType.equals(rhs.subordinationType))))&&((this.startedDate == rhs.startedDate)||((this.startedDate!= null)&&this.startedDate.equals(rhs.startedDate))))&&((this.uiStatus == rhs.uiStatus)||((this.uiStatus!= null)&&this.uiStatus.equals(rhs.uiStatus))))&&((this.userId == rhs.userId)||((this.userId!= null)&&this.userId.equals(rhs.userId))))&&((this.completedDate == rhs.completedDate)||((this.completedDate!= null)&&this.completedDate.equals(rhs.completedDate))))&&((this.parentJobId == rhs.parentJobId)||((this.parentJobId!= null)&&this.parentJobId.equals(rhs.parentJobId))))&&((this.jobProfileInfo == rhs.jobProfileInfo)||((this.jobProfileInfo!= null)&&this.jobProfileInfo.equals(rhs.jobProfileInfo))))&&((this.errorStatus == rhs.errorStatus)||((this.errorStatus!= null)&&this.errorStatus.equals(rhs.errorStatus))))&&((this.id == rhs.id)||((this.id!= null)&&this.id.equals(rhs.id))))&&((this.additionalProperties == rhs.additionalProperties)||((this.additionalProperties!= null)&&this.additionalProperties.equals(rhs.additionalProperties))))&&((this.sourcePath == rhs.sourcePath)||((this.sourcePath!= null)&&this.sourcePath.equals(rhs.sourcePath))))&&((this.status == rhs.status)||((this.status!= null)&&this.status.equals(rhs.status))));
    }


    /**
     * Type of subordination to another JobExecution entities
     * 
     */
    public enum SubordinationType {

        CHILD("CHILD"),
        PARENT_SINGLE("PARENT_SINGLE"),
        PARENT_MULTIPLE("PARENT_MULTIPLE");
        private final String value;
        private final static Map<String, JobExecution.SubordinationType> CONSTANTS = new HashMap<String, JobExecution.SubordinationType>();

        static {
            for (JobExecution.SubordinationType c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private SubordinationType(String value) {
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
        public static JobExecution.SubordinationType fromValue(String value) {
            JobExecution.SubordinationType constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }


    /**
     * JobExecution UI status Enum
     * 
     */
    public enum UiStatus {

        PARENT("PARENT"),
        INITIALIZATION("INITIALIZATION"),
        PREPARING_FOR_PREVIEW("PREPARING_FOR_PREVIEW"),
        READY_FOR_PREVIEW("READY_FOR_PREVIEW"),
        RUNNING("RUNNING"),
        RUNNING_COMPLETE("RUNNING_COMPLETE"),
        ERROR("ERROR"),
        DISCARDED("DISCARDED");
        private final String value;
        private final static Map<String, JobExecution.UiStatus> CONSTANTS = new HashMap<String, JobExecution.UiStatus>();

        static {
            for (JobExecution.UiStatus c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private UiStatus(String value) {
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
        public static JobExecution.UiStatus fromValue(String value) {
            JobExecution.UiStatus constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
