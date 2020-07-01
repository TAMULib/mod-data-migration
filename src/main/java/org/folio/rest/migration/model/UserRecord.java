package org.folio.rest.migration.model;

import org.folio.rest.jaxrs.model.Userdata;

public class UserRecord {

  private final String userId;

  public UserRecord(String userId) {
    this.userId = userId;
  }

  public String getUserId() {
    return userId;
  }

  public Userdata toUser() {
    final Userdata user = new Userdata();

    user.setId(userId);

    return user;
  }

}