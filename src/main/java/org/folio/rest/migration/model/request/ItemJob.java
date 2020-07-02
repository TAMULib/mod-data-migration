package org.folio.rest.migration.model.request;

import javax.validation.constraints.NotNull;

import org.folio.rest.jaxrs.model.common.ProfileInfo;

public class ItemJob extends AbstractJob {

    @NotNull
    private String userId;

    @NotNull
    private String materialTypeId;

    @NotNull
    private String itemRLTypeId;

    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMaterialTypeId() {
        return materialTypeId;
    }
    
    public void setMaterialTypeId(String materialTypeId) {
        this.materialTypeId = materialTypeId;
    }

    public String getItemRLTypeId() {
        return itemRLTypeId;
    }

    public void setItemRLTypeId(String itemRLTypeId) {
        this.itemRLTypeId = itemRLTypeId;
    }

}
