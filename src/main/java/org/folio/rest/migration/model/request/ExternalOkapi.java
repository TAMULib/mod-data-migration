package org.folio.rest.migration.model.request;

import javax.validation.constraints.NotNull;

import org.folio.rest.migration.config.model.Credentials;

public class ExternalOkapi {

  @NotNull
  private String url;

  @NotNull
  private String tenant;

  @NotNull
  private Credentials credentials;

  public ExternalOkapi() {

  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getTenant() {
    return tenant;
  }

  public void setTenant(String tenant) {
    this.tenant = tenant;
  }

  public Credentials getCredentials() {
    return credentials;
  }

  public void setCredentials(Credentials credentials) {
    this.credentials = credentials;
  }

}
