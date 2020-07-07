package org.folio.rest.migration.model;

import java.util.Date;

import org.folio.rest.jaxrs.model.Personal;
import org.folio.rest.jaxrs.model.Userdata;

public class UserRecord {

  private final String username;
  private final String id;
  private final String externalSystemId;
  private final String barcode;
  private final boolean active;
  private final String type;
  private final String patronGroup;
  private final Personal personal;
  private final Date enrollmentDate;
  private final Date expirationDate;

  public UserRecord(String username, String id, String externalSystemId, String barcode, boolean active, String type,
      String patronGroup, Personal personal, Date enrollmentDate, Date expirationDate) {
    this.username = username;
    this.id = id;
    this.externalSystemId = externalSystemId;
    this.barcode = barcode;
    this.active = active;
    this.type = type;
    this.patronGroup = patronGroup;
    this.personal = personal;
    this.enrollmentDate = enrollmentDate;
    this.expirationDate = expirationDate;
  }

  public String getUsername() {
    return username;
  }

  public String getId() {
    return id;
  }

  public String getExternalSystemId() {
    return externalSystemId;
  }

  public String getBarcode() {
    return barcode;
  }

  public boolean isActive() {
    return active;
  }

  public String getType() {
    return type;
  }

  public String getPatronGroup() {
    return patronGroup;
  }

  public Personal getPersonal() {
    return personal;
  }

  public Date getEnrollmentDate() {
    return enrollmentDate;
  }

  public Date getExpirationDate() {
    return expirationDate;
  }

  public Userdata toUser() {
    final Userdata user = new Userdata();

    user.setUsername(username);
    user.setId(id);
    user.setExternalSystemId(externalSystemId);
    user.setBarcode(barcode);
    user.setActive(active);
    user.setType(type);
    user.setPatronGroup(patronGroup);
    user.setPersonal(personal);
    user.setEnrollmentDate(enrollmentDate);
    user.setExpirationDate(expirationDate);

    return user;
  }

}