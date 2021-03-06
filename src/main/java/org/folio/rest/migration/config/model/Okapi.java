package org.folio.rest.migration.config.model;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "okapi")
public class Okapi {

  private String url;

  private Credentials credentials;

  private Modules modules;

  public Okapi() {

  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public Credentials getCredentials() {
    return credentials;
  }

  public void setCredentials(Credentials credentials) {
    this.credentials = credentials;
  }

  public Modules getModules() {
    return modules;
  }

  public void setModules(Modules modules) {
    this.modules = modules;
  }

}
