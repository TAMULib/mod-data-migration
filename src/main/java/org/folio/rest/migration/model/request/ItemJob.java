package org.folio.rest.migration.model.request;

import javax.validation.constraints.NotNull;

import org.folio.rest.jaxrs.model.common.ProfileInfo;

public class ItemJob extends AbstractJob {

    @NotNull
    private String userId;

    @NotNull
    private ProfileInfo profileInfo;
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public ProfileInfo getProfileInfo() {
        return profileInfo;
    }

    public void setProfileInfo(ProfileInfo profileInfo) {
        this.profileInfo = profileInfo;
    }
}