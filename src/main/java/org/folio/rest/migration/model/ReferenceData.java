package org.folio.rest.migration.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;

public class ReferenceData {

  private String path;

  // will be populated during processing
  @JsonIgnore
  private String name;

  // will be populated during processing
  @JsonIgnore
  private String filePath;

  // will be populated from request
  @JsonIgnore
  private String tenant;

  // will be populated during loading
  @JsonIgnore
  private String token;

  private List<String> dependencies;

  private List<JsonNode> data;

  public ReferenceData() {
    dependencies = new ArrayList<>();
    data = new ArrayList<>();
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getFilePath() {
    return filePath;
  }

  public void setFilePath(String filePath) {
    this.filePath = filePath;
  }

  public String getTenant() {
    return tenant;
  }

  public void setTenant(String tenant) {
    this.tenant = tenant;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public List<String> getDependencies() {
    return dependencies;
  }

  public void setDependencies(List<String> dependencies) {
    this.dependencies = dependencies;
  }

  public List<JsonNode> getData() {
    return data;
  }

  public void setData(List<JsonNode> data) {
    this.data = data;
  }

  public ReferenceData withPath(String path) {
    this.path = path;
    return this;
  }

  public ReferenceData withName(String name) {
    this.name = name;
    return this;
  }

  public ReferenceData withFilePath(String filePath) {
    this.filePath = filePath;
    return this;
  }

  public ReferenceData withTenant(String tenant) {
    this.tenant = tenant;
    return this;
  }

  public ReferenceData withToken(String token) {
    this.token = token;
    return this;
  }

}
