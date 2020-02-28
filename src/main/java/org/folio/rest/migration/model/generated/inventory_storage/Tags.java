
package org.folio.rest.migration.model.generated.inventory_storage;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


/**
 * tags
 * <p>
 * List of simple tags that can be added to an object
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "tagList"
})
public class Tags {

    /**
     * List of tags
     * 
     */
    @JsonProperty("tagList")
    @JsonPropertyDescription("List of tags")
    private List<String> tagList = new ArrayList<String>();

    /**
     * List of tags
     * 
     */
    @JsonProperty("tagList")
    public List<String> getTagList() {
        return tagList;
    }

    /**
     * List of tags
     * 
     */
    @JsonProperty("tagList")
    public void setTagList(List<String> tagList) {
        this.tagList = tagList;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Tags.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("tagList");
        sb.append('=');
        sb.append(((this.tagList == null)?"<null>":this.tagList));
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
        result = ((result* 31)+((this.tagList == null)? 0 :this.tagList.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Tags) == false) {
            return false;
        }
        Tags rhs = ((Tags) other);
        return ((this.tagList == rhs.tagList)||((this.tagList!= null)&&this.tagList.equals(rhs.tagList)));
    }

}
