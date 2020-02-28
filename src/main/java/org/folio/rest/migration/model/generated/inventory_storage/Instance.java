
package org.folio.rest.migration.model.generated.inventory_storage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;


/**
 * An instance record
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "hrid",
    "source",
    "title",
    "indexTitle",
    "alternativeTitles",
    "editions",
    "series",
    "identifiers",
    "contributors",
    "subjects",
    "classifications",
    "publication",
    "publicationFrequency",
    "publicationRange",
    "electronicAccess",
    "instanceTypeId",
    "instanceFormatIds",
    "physicalDescriptions",
    "languages",
    "notes",
    "modeOfIssuanceId",
    "catalogedDate",
    "previouslyHeld",
    "staffSuppress",
    "discoverySuppress",
    "statisticalCodeIds",
    "sourceRecordFormat",
    "statusId",
    "statusUpdatedDate",
    "tags",
    "metadata",
    "natureOfContentTermIds"
})
public class Instance {

    /**
     * Regexp pattern for UUID validation
     * 
     */
    @JsonProperty("id")
    @JsonPropertyDescription("Regexp pattern for UUID validation")
    private String id;
    /**
     * The human readable ID, also called eye readable ID. A system-assigned sequential ID which maps to the Instance ID
     * 
     */
    @JsonProperty("hrid")
    @JsonPropertyDescription("The human readable ID, also called eye readable ID. A system-assigned sequential ID which maps to the Instance ID")
    private String hrid;
    /**
     * The metadata source and its format of the underlying record to the instance record. (e.g. FOLIO if it's a record created in Inventory;  MARC if it's a MARC record created in MARCcat or EPKB if it's a record coming from eHoldings)
     * (Required)
     * 
     */
    @JsonProperty("source")
    @JsonPropertyDescription("The metadata source and its format of the underlying record to the instance record. (e.g. FOLIO if it's a record created in Inventory;  MARC if it's a MARC record created in MARCcat or EPKB if it's a record coming from eHoldings)")
    private String source;
    /**
     * The primary title (or label) associated with the resource
     * (Required)
     * 
     */
    @JsonProperty("title")
    @JsonPropertyDescription("The primary title (or label) associated with the resource")
    private String title;
    /**
     * Title normalized for browsing and searching; based on the title with articles removed
     * 
     */
    @JsonProperty("indexTitle")
    @JsonPropertyDescription("Title normalized for browsing and searching; based on the title with articles removed")
    private String indexTitle;
    /**
     * List of alternative titles for the resource (e.g. original language version title of a movie)
     * 
     */
    @JsonProperty("alternativeTitles")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    @JsonPropertyDescription("List of alternative titles for the resource (e.g. original language version title of a movie)")
    private Set<AlternativeTitle> alternativeTitles = new LinkedHashSet<AlternativeTitle>();
    /**
     * The edition statement, imprint and other publication source information
     * 
     */
    @JsonProperty("editions")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    @JsonPropertyDescription("The edition statement, imprint and other publication source information")
    private Set<String> editions = new LinkedHashSet<String>();
    /**
     * List of series titles associated with the resource (e.g. Harry Potter)
     * 
     */
    @JsonProperty("series")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    @JsonPropertyDescription("List of series titles associated with the resource (e.g. Harry Potter)")
    private Set<String> series = new LinkedHashSet<String>();
    /**
     * An extensible set of name-value pairs of identifiers associated with the resource
     * 
     */
    @JsonProperty("identifiers")
    @JsonPropertyDescription("An extensible set of name-value pairs of identifiers associated with the resource")
    private List<Identifier> identifiers = new ArrayList<Identifier>();
    /**
     * List of contributors
     * 
     */
    @JsonProperty("contributors")
    @JsonPropertyDescription("List of contributors")
    private List<Contributor> contributors = new ArrayList<Contributor>();
    /**
     * List of subject headings
     * 
     */
    @JsonProperty("subjects")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    @JsonPropertyDescription("List of subject headings")
    private Set<String> subjects = new LinkedHashSet<String>();
    /**
     * List of classifications
     * 
     */
    @JsonProperty("classifications")
    @JsonPropertyDescription("List of classifications")
    private List<Classification> classifications = new ArrayList<Classification>();
    /**
     * List of publication items
     * 
     */
    @JsonProperty("publication")
    @JsonPropertyDescription("List of publication items")
    private List<Publication> publication = new ArrayList<Publication>();
    /**
     * List of intervals at which a serial appears (e.g. daily, weekly, monthly, quarterly, etc.)
     * 
     */
    @JsonProperty("publicationFrequency")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    @JsonPropertyDescription("List of intervals at which a serial appears (e.g. daily, weekly, monthly, quarterly, etc.)")
    private Set<String> publicationFrequency = new LinkedHashSet<String>();
    /**
     * The range of sequential designation/chronology of publication, or date range
     * 
     */
    @JsonProperty("publicationRange")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    @JsonPropertyDescription("The range of sequential designation/chronology of publication, or date range")
    private Set<String> publicationRange = new LinkedHashSet<String>();
    /**
     * List of electronic access items
     * 
     */
    @JsonProperty("electronicAccess")
    @JsonPropertyDescription("List of electronic access items")
    private List<ElectronicAccess> electronicAccess = new ArrayList<ElectronicAccess>();
    /**
     * Regexp pattern for UUID validation
     * (Required)
     * 
     */
    @JsonProperty("instanceTypeId")
    @JsonPropertyDescription("Regexp pattern for UUID validation")
    private String instanceTypeId;
    /**
     * UUIDs for the unique terms for the format whether it's from the RDA carrier term list of locally defined
     * 
     */
    @JsonProperty("instanceFormatIds")
    @JsonPropertyDescription("UUIDs for the unique terms for the format whether it's from the RDA carrier term list of locally defined")
    private List<String> instanceFormatIds = new ArrayList<String>();
    /**
     * Physical description of the described resource, including its extent, dimensions, and such other physical details as a description of any accompanying materials and unit type and size
     * 
     */
    @JsonProperty("physicalDescriptions")
    @JsonPropertyDescription("Physical description of the described resource, including its extent, dimensions, and such other physical details as a description of any accompanying materials and unit type and size")
    private List<String> physicalDescriptions = new ArrayList<String>();
    /**
     * The set of languages used by the resource
     * 
     */
    @JsonProperty("languages")
    @JsonPropertyDescription("The set of languages used by the resource")
    private List<String> languages = new ArrayList<String>();
    /**
     * Bibliographic notes (e.g. general notes, specialized notes), and administrative notes
     * 
     */
    @JsonProperty("notes")
    @JsonPropertyDescription("Bibliographic notes (e.g. general notes, specialized notes), and administrative notes")
    private List<Note> notes = new ArrayList<Note>();
    /**
     * Regexp pattern for UUID validation
     * 
     */
    @JsonProperty("modeOfIssuanceId")
    @JsonPropertyDescription("Regexp pattern for UUID validation")
    private String modeOfIssuanceId;
    /**
     * Date or timestamp on an instance for when is was considered cataloged
     * 
     */
    @JsonProperty("catalogedDate")
    @JsonPropertyDescription("Date or timestamp on an instance for when is was considered cataloged")
    private String catalogedDate;
    /**
     * Records the fact that the resource was previously held by the library for things like Hathi access, etc.
     * 
     */
    @JsonProperty("previouslyHeld")
    @JsonPropertyDescription("Records the fact that the resource was previously held by the library for things like Hathi access, etc.")
    private Boolean previouslyHeld;
    /**
     * Records the fact that the record should not be displayed for others than catalogers
     * 
     */
    @JsonProperty("staffSuppress")
    @JsonPropertyDescription("Records the fact that the record should not be displayed for others than catalogers")
    private Boolean staffSuppress;
    /**
     * Records the fact that the record should not be displayed in a discovery system
     * 
     */
    @JsonProperty("discoverySuppress")
    @JsonPropertyDescription("Records the fact that the record should not be displayed in a discovery system")
    private Boolean discoverySuppress;
    /**
     * List of statistical code IDs
     * 
     */
    @JsonProperty("statisticalCodeIds")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    @JsonPropertyDescription("List of statistical code IDs")
    private Set<String> statisticalCodeIds = new LinkedHashSet<String>();
    /**
     * Format of the instance source record, if a source record exists (e.g. FOLIO if it's a record created in Inventory,  MARC if it's a MARC record created in MARCcat or EPKB if it's a record coming from eHoldings)
     * 
     */
    @JsonProperty("sourceRecordFormat")
    @JsonPropertyDescription("Format of the instance source record, if a source record exists (e.g. FOLIO if it's a record created in Inventory,  MARC if it's a MARC record created in MARCcat or EPKB if it's a record coming from eHoldings)")
    private Instance.SourceRecordFormat sourceRecordFormat;
    /**
     * Regexp pattern for UUID validation
     * 
     */
    @JsonProperty("statusId")
    @JsonPropertyDescription("Regexp pattern for UUID validation")
    private String statusId;
    /**
     * Date [or timestamp] for when the instance status was updated
     * 
     */
    @JsonProperty("statusUpdatedDate")
    @JsonPropertyDescription("Date [or timestamp] for when the instance status was updated")
    private String statusUpdatedDate;
    /**
     * tags
     * <p>
     * List of simple tags that can be added to an object
     * 
     */
    @JsonProperty("tags")
    @JsonPropertyDescription("List of simple tags that can be added to an object")
    private Tags tags;
    /**
     * Metadata Schema
     * <p>
     * Metadata about creation and changes to records, provided by the server (client should not provide)
     * 
     */
    @JsonProperty("metadata")
    @JsonPropertyDescription("Metadata about creation and changes to records, provided by the server (client should not provide)")
    private Metadata metadata;
    /**
     * Array of UUID for the Instance nature of content (e.g. bibliography, biography, exhibition catalogue, festschrift, newspaper, proceedings, research report, thesis or website)
     * 
     */
    @JsonProperty("natureOfContentTermIds")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    @JsonPropertyDescription("Array of UUID for the Instance nature of content (e.g. bibliography, biography, exhibition catalogue, festschrift, newspaper, proceedings, research report, thesis or website)")
    private Set<String> natureOfContentTermIds = new LinkedHashSet<String>();

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
     * The human readable ID, also called eye readable ID. A system-assigned sequential ID which maps to the Instance ID
     * 
     */
    @JsonProperty("hrid")
    public String getHrid() {
        return hrid;
    }

    /**
     * The human readable ID, also called eye readable ID. A system-assigned sequential ID which maps to the Instance ID
     * 
     */
    @JsonProperty("hrid")
    public void setHrid(String hrid) {
        this.hrid = hrid;
    }

    /**
     * The metadata source and its format of the underlying record to the instance record. (e.g. FOLIO if it's a record created in Inventory;  MARC if it's a MARC record created in MARCcat or EPKB if it's a record coming from eHoldings)
     * (Required)
     * 
     */
    @JsonProperty("source")
    public String getSource() {
        return source;
    }

    /**
     * The metadata source and its format of the underlying record to the instance record. (e.g. FOLIO if it's a record created in Inventory;  MARC if it's a MARC record created in MARCcat or EPKB if it's a record coming from eHoldings)
     * (Required)
     * 
     */
    @JsonProperty("source")
    public void setSource(String source) {
        this.source = source;
    }

    /**
     * The primary title (or label) associated with the resource
     * (Required)
     * 
     */
    @JsonProperty("title")
    public String getTitle() {
        return title;
    }

    /**
     * The primary title (or label) associated with the resource
     * (Required)
     * 
     */
    @JsonProperty("title")
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Title normalized for browsing and searching; based on the title with articles removed
     * 
     */
    @JsonProperty("indexTitle")
    public String getIndexTitle() {
        return indexTitle;
    }

    /**
     * Title normalized for browsing and searching; based on the title with articles removed
     * 
     */
    @JsonProperty("indexTitle")
    public void setIndexTitle(String indexTitle) {
        this.indexTitle = indexTitle;
    }

    /**
     * List of alternative titles for the resource (e.g. original language version title of a movie)
     * 
     */
    @JsonProperty("alternativeTitles")
    public Set<AlternativeTitle> getAlternativeTitles() {
        return alternativeTitles;
    }

    /**
     * List of alternative titles for the resource (e.g. original language version title of a movie)
     * 
     */
    @JsonProperty("alternativeTitles")
    public void setAlternativeTitles(Set<AlternativeTitle> alternativeTitles) {
        this.alternativeTitles = alternativeTitles;
    }

    /**
     * The edition statement, imprint and other publication source information
     * 
     */
    @JsonProperty("editions")
    public Set<String> getEditions() {
        return editions;
    }

    /**
     * The edition statement, imprint and other publication source information
     * 
     */
    @JsonProperty("editions")
    public void setEditions(Set<String> editions) {
        this.editions = editions;
    }

    /**
     * List of series titles associated with the resource (e.g. Harry Potter)
     * 
     */
    @JsonProperty("series")
    public Set<String> getSeries() {
        return series;
    }

    /**
     * List of series titles associated with the resource (e.g. Harry Potter)
     * 
     */
    @JsonProperty("series")
    public void setSeries(Set<String> series) {
        this.series = series;
    }

    /**
     * An extensible set of name-value pairs of identifiers associated with the resource
     * 
     */
    @JsonProperty("identifiers")
    public List<Identifier> getIdentifiers() {
        return identifiers;
    }

    /**
     * An extensible set of name-value pairs of identifiers associated with the resource
     * 
     */
    @JsonProperty("identifiers")
    public void setIdentifiers(List<Identifier> identifiers) {
        this.identifiers = identifiers;
    }

    /**
     * List of contributors
     * 
     */
    @JsonProperty("contributors")
    public List<Contributor> getContributors() {
        return contributors;
    }

    /**
     * List of contributors
     * 
     */
    @JsonProperty("contributors")
    public void setContributors(List<Contributor> contributors) {
        this.contributors = contributors;
    }

    /**
     * List of subject headings
     * 
     */
    @JsonProperty("subjects")
    public Set<String> getSubjects() {
        return subjects;
    }

    /**
     * List of subject headings
     * 
     */
    @JsonProperty("subjects")
    public void setSubjects(Set<String> subjects) {
        this.subjects = subjects;
    }

    /**
     * List of classifications
     * 
     */
    @JsonProperty("classifications")
    public List<Classification> getClassifications() {
        return classifications;
    }

    /**
     * List of classifications
     * 
     */
    @JsonProperty("classifications")
    public void setClassifications(List<Classification> classifications) {
        this.classifications = classifications;
    }

    /**
     * List of publication items
     * 
     */
    @JsonProperty("publication")
    public List<Publication> getPublication() {
        return publication;
    }

    /**
     * List of publication items
     * 
     */
    @JsonProperty("publication")
    public void setPublication(List<Publication> publication) {
        this.publication = publication;
    }

    /**
     * List of intervals at which a serial appears (e.g. daily, weekly, monthly, quarterly, etc.)
     * 
     */
    @JsonProperty("publicationFrequency")
    public Set<String> getPublicationFrequency() {
        return publicationFrequency;
    }

    /**
     * List of intervals at which a serial appears (e.g. daily, weekly, monthly, quarterly, etc.)
     * 
     */
    @JsonProperty("publicationFrequency")
    public void setPublicationFrequency(Set<String> publicationFrequency) {
        this.publicationFrequency = publicationFrequency;
    }

    /**
     * The range of sequential designation/chronology of publication, or date range
     * 
     */
    @JsonProperty("publicationRange")
    public Set<String> getPublicationRange() {
        return publicationRange;
    }

    /**
     * The range of sequential designation/chronology of publication, or date range
     * 
     */
    @JsonProperty("publicationRange")
    public void setPublicationRange(Set<String> publicationRange) {
        this.publicationRange = publicationRange;
    }

    /**
     * List of electronic access items
     * 
     */
    @JsonProperty("electronicAccess")
    public List<ElectronicAccess> getElectronicAccess() {
        return electronicAccess;
    }

    /**
     * List of electronic access items
     * 
     */
    @JsonProperty("electronicAccess")
    public void setElectronicAccess(List<ElectronicAccess> electronicAccess) {
        this.electronicAccess = electronicAccess;
    }

    /**
     * Regexp pattern for UUID validation
     * (Required)
     * 
     */
    @JsonProperty("instanceTypeId")
    public String getInstanceTypeId() {
        return instanceTypeId;
    }

    /**
     * Regexp pattern for UUID validation
     * (Required)
     * 
     */
    @JsonProperty("instanceTypeId")
    public void setInstanceTypeId(String instanceTypeId) {
        this.instanceTypeId = instanceTypeId;
    }

    /**
     * UUIDs for the unique terms for the format whether it's from the RDA carrier term list of locally defined
     * 
     */
    @JsonProperty("instanceFormatIds")
    public List<String> getInstanceFormatIds() {
        return instanceFormatIds;
    }

    /**
     * UUIDs for the unique terms for the format whether it's from the RDA carrier term list of locally defined
     * 
     */
    @JsonProperty("instanceFormatIds")
    public void setInstanceFormatIds(List<String> instanceFormatIds) {
        this.instanceFormatIds = instanceFormatIds;
    }

    /**
     * Physical description of the described resource, including its extent, dimensions, and such other physical details as a description of any accompanying materials and unit type and size
     * 
     */
    @JsonProperty("physicalDescriptions")
    public List<String> getPhysicalDescriptions() {
        return physicalDescriptions;
    }

    /**
     * Physical description of the described resource, including its extent, dimensions, and such other physical details as a description of any accompanying materials and unit type and size
     * 
     */
    @JsonProperty("physicalDescriptions")
    public void setPhysicalDescriptions(List<String> physicalDescriptions) {
        this.physicalDescriptions = physicalDescriptions;
    }

    /**
     * The set of languages used by the resource
     * 
     */
    @JsonProperty("languages")
    public List<String> getLanguages() {
        return languages;
    }

    /**
     * The set of languages used by the resource
     * 
     */
    @JsonProperty("languages")
    public void setLanguages(List<String> languages) {
        this.languages = languages;
    }

    /**
     * Bibliographic notes (e.g. general notes, specialized notes), and administrative notes
     * 
     */
    @JsonProperty("notes")
    public List<Note> getNotes() {
        return notes;
    }

    /**
     * Bibliographic notes (e.g. general notes, specialized notes), and administrative notes
     * 
     */
    @JsonProperty("notes")
    public void setNotes(List<Note> notes) {
        this.notes = notes;
    }

    /**
     * Regexp pattern for UUID validation
     * 
     */
    @JsonProperty("modeOfIssuanceId")
    public String getModeOfIssuanceId() {
        return modeOfIssuanceId;
    }

    /**
     * Regexp pattern for UUID validation
     * 
     */
    @JsonProperty("modeOfIssuanceId")
    public void setModeOfIssuanceId(String modeOfIssuanceId) {
        this.modeOfIssuanceId = modeOfIssuanceId;
    }

    /**
     * Date or timestamp on an instance for when is was considered cataloged
     * 
     */
    @JsonProperty("catalogedDate")
    public String getCatalogedDate() {
        return catalogedDate;
    }

    /**
     * Date or timestamp on an instance for when is was considered cataloged
     * 
     */
    @JsonProperty("catalogedDate")
    public void setCatalogedDate(String catalogedDate) {
        this.catalogedDate = catalogedDate;
    }

    /**
     * Records the fact that the resource was previously held by the library for things like Hathi access, etc.
     * 
     */
    @JsonProperty("previouslyHeld")
    public Boolean getPreviouslyHeld() {
        return previouslyHeld;
    }

    /**
     * Records the fact that the resource was previously held by the library for things like Hathi access, etc.
     * 
     */
    @JsonProperty("previouslyHeld")
    public void setPreviouslyHeld(Boolean previouslyHeld) {
        this.previouslyHeld = previouslyHeld;
    }

    /**
     * Records the fact that the record should not be displayed for others than catalogers
     * 
     */
    @JsonProperty("staffSuppress")
    public Boolean getStaffSuppress() {
        return staffSuppress;
    }

    /**
     * Records the fact that the record should not be displayed for others than catalogers
     * 
     */
    @JsonProperty("staffSuppress")
    public void setStaffSuppress(Boolean staffSuppress) {
        this.staffSuppress = staffSuppress;
    }

    /**
     * Records the fact that the record should not be displayed in a discovery system
     * 
     */
    @JsonProperty("discoverySuppress")
    public Boolean getDiscoverySuppress() {
        return discoverySuppress;
    }

    /**
     * Records the fact that the record should not be displayed in a discovery system
     * 
     */
    @JsonProperty("discoverySuppress")
    public void setDiscoverySuppress(Boolean discoverySuppress) {
        this.discoverySuppress = discoverySuppress;
    }

    /**
     * List of statistical code IDs
     * 
     */
    @JsonProperty("statisticalCodeIds")
    public Set<String> getStatisticalCodeIds() {
        return statisticalCodeIds;
    }

    /**
     * List of statistical code IDs
     * 
     */
    @JsonProperty("statisticalCodeIds")
    public void setStatisticalCodeIds(Set<String> statisticalCodeIds) {
        this.statisticalCodeIds = statisticalCodeIds;
    }

    /**
     * Format of the instance source record, if a source record exists (e.g. FOLIO if it's a record created in Inventory,  MARC if it's a MARC record created in MARCcat or EPKB if it's a record coming from eHoldings)
     * 
     */
    @JsonProperty("sourceRecordFormat")
    public Instance.SourceRecordFormat getSourceRecordFormat() {
        return sourceRecordFormat;
    }

    /**
     * Format of the instance source record, if a source record exists (e.g. FOLIO if it's a record created in Inventory,  MARC if it's a MARC record created in MARCcat or EPKB if it's a record coming from eHoldings)
     * 
     */
    @JsonProperty("sourceRecordFormat")
    public void setSourceRecordFormat(Instance.SourceRecordFormat sourceRecordFormat) {
        this.sourceRecordFormat = sourceRecordFormat;
    }

    /**
     * Regexp pattern for UUID validation
     * 
     */
    @JsonProperty("statusId")
    public String getStatusId() {
        return statusId;
    }

    /**
     * Regexp pattern for UUID validation
     * 
     */
    @JsonProperty("statusId")
    public void setStatusId(String statusId) {
        this.statusId = statusId;
    }

    /**
     * Date [or timestamp] for when the instance status was updated
     * 
     */
    @JsonProperty("statusUpdatedDate")
    public String getStatusUpdatedDate() {
        return statusUpdatedDate;
    }

    /**
     * Date [or timestamp] for when the instance status was updated
     * 
     */
    @JsonProperty("statusUpdatedDate")
    public void setStatusUpdatedDate(String statusUpdatedDate) {
        this.statusUpdatedDate = statusUpdatedDate;
    }

    /**
     * tags
     * <p>
     * List of simple tags that can be added to an object
     * 
     */
    @JsonProperty("tags")
    public Tags getTags() {
        return tags;
    }

    /**
     * tags
     * <p>
     * List of simple tags that can be added to an object
     * 
     */
    @JsonProperty("tags")
    public void setTags(Tags tags) {
        this.tags = tags;
    }

    /**
     * Metadata Schema
     * <p>
     * Metadata about creation and changes to records, provided by the server (client should not provide)
     * 
     */
    @JsonProperty("metadata")
    public Metadata getMetadata() {
        return metadata;
    }

    /**
     * Metadata Schema
     * <p>
     * Metadata about creation and changes to records, provided by the server (client should not provide)
     * 
     */
    @JsonProperty("metadata")
    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    /**
     * Array of UUID for the Instance nature of content (e.g. bibliography, biography, exhibition catalogue, festschrift, newspaper, proceedings, research report, thesis or website)
     * 
     */
    @JsonProperty("natureOfContentTermIds")
    public Set<String> getNatureOfContentTermIds() {
        return natureOfContentTermIds;
    }

    /**
     * Array of UUID for the Instance nature of content (e.g. bibliography, biography, exhibition catalogue, festschrift, newspaper, proceedings, research report, thesis or website)
     * 
     */
    @JsonProperty("natureOfContentTermIds")
    public void setNatureOfContentTermIds(Set<String> natureOfContentTermIds) {
        this.natureOfContentTermIds = natureOfContentTermIds;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Instance.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("id");
        sb.append('=');
        sb.append(((this.id == null)?"<null>":this.id));
        sb.append(',');
        sb.append("hrid");
        sb.append('=');
        sb.append(((this.hrid == null)?"<null>":this.hrid));
        sb.append(',');
        sb.append("source");
        sb.append('=');
        sb.append(((this.source == null)?"<null>":this.source));
        sb.append(',');
        sb.append("title");
        sb.append('=');
        sb.append(((this.title == null)?"<null>":this.title));
        sb.append(',');
        sb.append("indexTitle");
        sb.append('=');
        sb.append(((this.indexTitle == null)?"<null>":this.indexTitle));
        sb.append(',');
        sb.append("alternativeTitles");
        sb.append('=');
        sb.append(((this.alternativeTitles == null)?"<null>":this.alternativeTitles));
        sb.append(',');
        sb.append("editions");
        sb.append('=');
        sb.append(((this.editions == null)?"<null>":this.editions));
        sb.append(',');
        sb.append("series");
        sb.append('=');
        sb.append(((this.series == null)?"<null>":this.series));
        sb.append(',');
        sb.append("identifiers");
        sb.append('=');
        sb.append(((this.identifiers == null)?"<null>":this.identifiers));
        sb.append(',');
        sb.append("contributors");
        sb.append('=');
        sb.append(((this.contributors == null)?"<null>":this.contributors));
        sb.append(',');
        sb.append("subjects");
        sb.append('=');
        sb.append(((this.subjects == null)?"<null>":this.subjects));
        sb.append(',');
        sb.append("classifications");
        sb.append('=');
        sb.append(((this.classifications == null)?"<null>":this.classifications));
        sb.append(',');
        sb.append("publication");
        sb.append('=');
        sb.append(((this.publication == null)?"<null>":this.publication));
        sb.append(',');
        sb.append("publicationFrequency");
        sb.append('=');
        sb.append(((this.publicationFrequency == null)?"<null>":this.publicationFrequency));
        sb.append(',');
        sb.append("publicationRange");
        sb.append('=');
        sb.append(((this.publicationRange == null)?"<null>":this.publicationRange));
        sb.append(',');
        sb.append("electronicAccess");
        sb.append('=');
        sb.append(((this.electronicAccess == null)?"<null>":this.electronicAccess));
        sb.append(',');
        sb.append("instanceTypeId");
        sb.append('=');
        sb.append(((this.instanceTypeId == null)?"<null>":this.instanceTypeId));
        sb.append(',');
        sb.append("instanceFormatIds");
        sb.append('=');
        sb.append(((this.instanceFormatIds == null)?"<null>":this.instanceFormatIds));
        sb.append(',');
        sb.append("physicalDescriptions");
        sb.append('=');
        sb.append(((this.physicalDescriptions == null)?"<null>":this.physicalDescriptions));
        sb.append(',');
        sb.append("languages");
        sb.append('=');
        sb.append(((this.languages == null)?"<null>":this.languages));
        sb.append(',');
        sb.append("notes");
        sb.append('=');
        sb.append(((this.notes == null)?"<null>":this.notes));
        sb.append(',');
        sb.append("modeOfIssuanceId");
        sb.append('=');
        sb.append(((this.modeOfIssuanceId == null)?"<null>":this.modeOfIssuanceId));
        sb.append(',');
        sb.append("catalogedDate");
        sb.append('=');
        sb.append(((this.catalogedDate == null)?"<null>":this.catalogedDate));
        sb.append(',');
        sb.append("previouslyHeld");
        sb.append('=');
        sb.append(((this.previouslyHeld == null)?"<null>":this.previouslyHeld));
        sb.append(',');
        sb.append("staffSuppress");
        sb.append('=');
        sb.append(((this.staffSuppress == null)?"<null>":this.staffSuppress));
        sb.append(',');
        sb.append("discoverySuppress");
        sb.append('=');
        sb.append(((this.discoverySuppress == null)?"<null>":this.discoverySuppress));
        sb.append(',');
        sb.append("statisticalCodeIds");
        sb.append('=');
        sb.append(((this.statisticalCodeIds == null)?"<null>":this.statisticalCodeIds));
        sb.append(',');
        sb.append("sourceRecordFormat");
        sb.append('=');
        sb.append(((this.sourceRecordFormat == null)?"<null>":this.sourceRecordFormat));
        sb.append(',');
        sb.append("statusId");
        sb.append('=');
        sb.append(((this.statusId == null)?"<null>":this.statusId));
        sb.append(',');
        sb.append("statusUpdatedDate");
        sb.append('=');
        sb.append(((this.statusUpdatedDate == null)?"<null>":this.statusUpdatedDate));
        sb.append(',');
        sb.append("tags");
        sb.append('=');
        sb.append(((this.tags == null)?"<null>":this.tags));
        sb.append(',');
        sb.append("metadata");
        sb.append('=');
        sb.append(((this.metadata == null)?"<null>":this.metadata));
        sb.append(',');
        sb.append("natureOfContentTermIds");
        sb.append('=');
        sb.append(((this.natureOfContentTermIds == null)?"<null>":this.natureOfContentTermIds));
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
        result = ((result* 31)+((this.metadata == null)? 0 :this.metadata.hashCode()));
        result = ((result* 31)+((this.notes == null)? 0 :this.notes.hashCode()));
        result = ((result* 31)+((this.previouslyHeld == null)? 0 :this.previouslyHeld.hashCode()));
        result = ((result* 31)+((this.modeOfIssuanceId == null)? 0 :this.modeOfIssuanceId.hashCode()));
        result = ((result* 31)+((this.catalogedDate == null)? 0 :this.catalogedDate.hashCode()));
        result = ((result* 31)+((this.source == null)? 0 :this.source.hashCode()));
        result = ((result* 31)+((this.title == null)? 0 :this.title.hashCode()));
        result = ((result* 31)+((this.indexTitle == null)? 0 :this.indexTitle.hashCode()));
        result = ((result* 31)+((this.publicationFrequency == null)? 0 :this.publicationFrequency.hashCode()));
        result = ((result* 31)+((this.electronicAccess == null)? 0 :this.electronicAccess.hashCode()));
        result = ((result* 31)+((this.statisticalCodeIds == null)? 0 :this.statisticalCodeIds.hashCode()));
        result = ((result* 31)+((this.statusUpdatedDate == null)? 0 :this.statusUpdatedDate.hashCode()));
        result = ((result* 31)+((this.natureOfContentTermIds == null)? 0 :this.natureOfContentTermIds.hashCode()));
        result = ((result* 31)+((this.hrid == null)? 0 :this.hrid.hashCode()));
        result = ((result* 31)+((this.instanceFormatIds == null)? 0 :this.instanceFormatIds.hashCode()));
        result = ((result* 31)+((this.publication == null)? 0 :this.publication.hashCode()));
        result = ((result* 31)+((this.sourceRecordFormat == null)? 0 :this.sourceRecordFormat.hashCode()));
        result = ((result* 31)+((this.id == null)? 0 :this.id.hashCode()));
        result = ((result* 31)+((this.alternativeTitles == null)? 0 :this.alternativeTitles.hashCode()));
        result = ((result* 31)+((this.physicalDescriptions == null)? 0 :this.physicalDescriptions.hashCode()));
        result = ((result* 31)+((this.languages == null)? 0 :this.languages.hashCode()));
        result = ((result* 31)+((this.identifiers == null)? 0 :this.identifiers.hashCode()));
        result = ((result* 31)+((this.instanceTypeId == null)? 0 :this.instanceTypeId.hashCode()));
        result = ((result* 31)+((this.subjects == null)? 0 :this.subjects.hashCode()));
        result = ((result* 31)+((this.tags == null)? 0 :this.tags.hashCode()));
        result = ((result* 31)+((this.classifications == null)? 0 :this.classifications.hashCode()));
        result = ((result* 31)+((this.publicationRange == null)? 0 :this.publicationRange.hashCode()));
        result = ((result* 31)+((this.editions == null)? 0 :this.editions.hashCode()));
        result = ((result* 31)+((this.discoverySuppress == null)? 0 :this.discoverySuppress.hashCode()));
        result = ((result* 31)+((this.statusId == null)? 0 :this.statusId.hashCode()));
        result = ((result* 31)+((this.series == null)? 0 :this.series.hashCode()));
        result = ((result* 31)+((this.staffSuppress == null)? 0 :this.staffSuppress.hashCode()));
        result = ((result* 31)+((this.contributors == null)? 0 :this.contributors.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Instance) == false) {
            return false;
        }
        Instance rhs = ((Instance) other);
        return ((((((((((((((((((((((((((((((((((this.metadata == rhs.metadata)||((this.metadata!= null)&&this.metadata.equals(rhs.metadata)))&&((this.notes == rhs.notes)||((this.notes!= null)&&this.notes.equals(rhs.notes))))&&((this.previouslyHeld == rhs.previouslyHeld)||((this.previouslyHeld!= null)&&this.previouslyHeld.equals(rhs.previouslyHeld))))&&((this.modeOfIssuanceId == rhs.modeOfIssuanceId)||((this.modeOfIssuanceId!= null)&&this.modeOfIssuanceId.equals(rhs.modeOfIssuanceId))))&&((this.catalogedDate == rhs.catalogedDate)||((this.catalogedDate!= null)&&this.catalogedDate.equals(rhs.catalogedDate))))&&((this.source == rhs.source)||((this.source!= null)&&this.source.equals(rhs.source))))&&((this.title == rhs.title)||((this.title!= null)&&this.title.equals(rhs.title))))&&((this.indexTitle == rhs.indexTitle)||((this.indexTitle!= null)&&this.indexTitle.equals(rhs.indexTitle))))&&((this.publicationFrequency == rhs.publicationFrequency)||((this.publicationFrequency!= null)&&this.publicationFrequency.equals(rhs.publicationFrequency))))&&((this.electronicAccess == rhs.electronicAccess)||((this.electronicAccess!= null)&&this.electronicAccess.equals(rhs.electronicAccess))))&&((this.statisticalCodeIds == rhs.statisticalCodeIds)||((this.statisticalCodeIds!= null)&&this.statisticalCodeIds.equals(rhs.statisticalCodeIds))))&&((this.statusUpdatedDate == rhs.statusUpdatedDate)||((this.statusUpdatedDate!= null)&&this.statusUpdatedDate.equals(rhs.statusUpdatedDate))))&&((this.natureOfContentTermIds == rhs.natureOfContentTermIds)||((this.natureOfContentTermIds!= null)&&this.natureOfContentTermIds.equals(rhs.natureOfContentTermIds))))&&((this.hrid == rhs.hrid)||((this.hrid!= null)&&this.hrid.equals(rhs.hrid))))&&((this.instanceFormatIds == rhs.instanceFormatIds)||((this.instanceFormatIds!= null)&&this.instanceFormatIds.equals(rhs.instanceFormatIds))))&&((this.publication == rhs.publication)||((this.publication!= null)&&this.publication.equals(rhs.publication))))&&((this.sourceRecordFormat == rhs.sourceRecordFormat)||((this.sourceRecordFormat!= null)&&this.sourceRecordFormat.equals(rhs.sourceRecordFormat))))&&((this.id == rhs.id)||((this.id!= null)&&this.id.equals(rhs.id))))&&((this.alternativeTitles == rhs.alternativeTitles)||((this.alternativeTitles!= null)&&this.alternativeTitles.equals(rhs.alternativeTitles))))&&((this.physicalDescriptions == rhs.physicalDescriptions)||((this.physicalDescriptions!= null)&&this.physicalDescriptions.equals(rhs.physicalDescriptions))))&&((this.languages == rhs.languages)||((this.languages!= null)&&this.languages.equals(rhs.languages))))&&((this.identifiers == rhs.identifiers)||((this.identifiers!= null)&&this.identifiers.equals(rhs.identifiers))))&&((this.instanceTypeId == rhs.instanceTypeId)||((this.instanceTypeId!= null)&&this.instanceTypeId.equals(rhs.instanceTypeId))))&&((this.subjects == rhs.subjects)||((this.subjects!= null)&&this.subjects.equals(rhs.subjects))))&&((this.tags == rhs.tags)||((this.tags!= null)&&this.tags.equals(rhs.tags))))&&((this.classifications == rhs.classifications)||((this.classifications!= null)&&this.classifications.equals(rhs.classifications))))&&((this.publicationRange == rhs.publicationRange)||((this.publicationRange!= null)&&this.publicationRange.equals(rhs.publicationRange))))&&((this.editions == rhs.editions)||((this.editions!= null)&&this.editions.equals(rhs.editions))))&&((this.discoverySuppress == rhs.discoverySuppress)||((this.discoverySuppress!= null)&&this.discoverySuppress.equals(rhs.discoverySuppress))))&&((this.statusId == rhs.statusId)||((this.statusId!= null)&&this.statusId.equals(rhs.statusId))))&&((this.series == rhs.series)||((this.series!= null)&&this.series.equals(rhs.series))))&&((this.staffSuppress == rhs.staffSuppress)||((this.staffSuppress!= null)&&this.staffSuppress.equals(rhs.staffSuppress))))&&((this.contributors == rhs.contributors)||((this.contributors!= null)&&this.contributors.equals(rhs.contributors))));
    }


    /**
     * Format of the instance source record, if a source record exists (e.g. FOLIO if it's a record created in Inventory,  MARC if it's a MARC record created in MARCcat or EPKB if it's a record coming from eHoldings)
     * 
     */
    public enum SourceRecordFormat {

        MARC_JSON("MARC-JSON");
        private final String value;
        private final static Map<String, Instance.SourceRecordFormat> CONSTANTS = new HashMap<String, Instance.SourceRecordFormat>();

        static {
            for (Instance.SourceRecordFormat c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private SourceRecordFormat(String value) {
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
        public static Instance.SourceRecordFormat fromValue(String value) {
            Instance.SourceRecordFormat constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
