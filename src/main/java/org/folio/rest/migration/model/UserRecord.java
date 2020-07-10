package org.folio.rest.migration.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;

import org.folio.rest.jaxrs.model.Address;
import org.folio.rest.jaxrs.model.Personal;
import org.folio.rest.jaxrs.model.Userdata;

public class UserRecord extends AbstractUserRecord {

  private static final String PATRON = "patron";

  private static final String EMAIL = "email";
  private static final String MAIL = "mail";
  private static final String TEXT = "text";

  private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("YYYYMMDD");
  {
    // TODO: the timezone may need to be changed.
    DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
  }

  private final String referenceId;
  private final String patronId;
  private final String institutionId;
  private final String lastName;
  private final String firstName;
  private final String middleName;
  private final String activeDate;
  private final String expireDate;
  private final String smsNumber;
  private final String currentCharges;

  private String addressStatus;
  private String addressType;
  private String barcode;
  private String email;
  private String groupcode;
  private String phone;
  private String mobilePhone;
  private String preferredContactTypeId;
  private String schema;
  private String username;

  private List<Address> addresses;

  public UserRecord(String referenceId, String patronId, String institutionId, String lastName, String firstName, String middleName, String activeDate, String expireDate, String smsNumber, String currentCharges) {
    this.referenceId = referenceId;
    this.patronId = patronId;
    this.institutionId = institutionId;
    this.lastName = lastName;
    this.firstName = firstName;
    this.middleName = middleName;
    this.activeDate = activeDate;
    this.expireDate = expireDate;
    this.smsNumber = smsNumber;
    this.currentCharges = currentCharges;

    schema = "";

    addresses = new ArrayList<>();
  }

  public String getReferenceId() {
    return referenceId;
  }

  public String getPatronId() {
    return patronId;
  }

  public String getInstitutionId() {
    return institutionId;
  }

  public String getLastName() {
    return lastName;
  }

  public String getFirstName() {
    return firstName;
  }

  public String getMiddleName() {
    return middleName;
  }

  public String getActiveDate() {
    return activeDate;
  }

  public String getExpireDate() {
    return expireDate;
  }

  public String getSmsNumber() {
    return smsNumber;
  }

  public String getCurrentCharges() {
    return currentCharges;
  }

  public String getAddressStatus() {
    return addressStatus;
  }

  public void setAddressStatus(String addressStatus) {
    this.addressStatus = addressStatus;
  }

  public String getAddressType() {
    return addressType;
  }

  public void setAddressType(String addressType) {
    this.addressType = addressType;
  }

  public String getBarcode() {
    return barcode;
  }

  public void setBarcode(String barcode) {
    this.barcode = barcode;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getGroupcode() {
    return groupcode;
  }

  public void setGroupcode(String groupcode) {
    this.groupcode = groupcode;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public String getMobilePhone() {
    return mobilePhone;
  }

  public void setMobilePhone(String mobilePhone) {
    this.mobilePhone = mobilePhone;
  }

  public String getPreferredContactTypeId() {
    return preferredContactTypeId;
  }

  public void setPreferredContactTypeId(String preferredContactTypeId) {
    this.preferredContactTypeId = preferredContactTypeId;
  }

  public String getSchema() {
    return schema;
  }

  public void setSchema(String schema) {
    this.schema = schema;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public List<Address> getAddresses() {
    return addresses;
  }

  public void addAddress(Address address) {
    addresses.add(address);
  }

  public Userdata toUserdata(String patronGroup) {
    final Userdata userdata = new Userdata();
    final Personal personal = new Personal();

    personal.setAddresses(addresses);

    setLastName(personal);
    setFirstName(personal);
    setMiddleName(personal);
    setEmail(personal);
    setPhone(personal);
    setMobilePhone(personal);
    setPreferredContactTypeId(personal);

    userdata.setId(referenceId);
    userdata.setPersonal(personal);
    userdata.setType(PATRON);

    setExternalSystemId(userdata);
    setActive(userdata);
    setBarcode(userdata);
    setUsername(userdata);

    userdata.setPatronGroup(patronGroup);

    return userdata;
  }

  private void setLastName(Personal personal) {
    if (Objects.nonNull(lastName)) {
      personal.setLastName(lastName);
    }
  }

  private void setFirstName(Personal personal) {
    if (Objects.nonNull(firstName)) {
      personal.setFirstName(firstName);
    }
  }

  private void setMiddleName(Personal personal) {
    if (Objects.nonNull(middleName)) {
      personal.setMiddleName(middleName);
    }
  }

  private void setEmail(Personal personal) {
    if (Objects.nonNull(email)) {
      personal.setEmail(email);
    }
  }

  private void setPhone(Personal personal) {
    if (Objects.nonNull(phone)) {
      personal.setPhone(phone);
    }
  }

  private void setMobilePhone(Personal personal) {
    if (Objects.nonNull(mobilePhone)) {
      personal.setMobilePhone(mobilePhone);
    }
  }

  private void setPreferredContactTypeId(Personal personal) {
    if (Objects.nonNull(smsNumber)) {
      preferredContactTypeId = TEXT;
      personal.setPreferredContactTypeId(preferredContactTypeId);
    }

    if (Objects.isNull(preferredContactTypeId)) {
      if (Objects.nonNull(addressType) && Objects.nonNull(addressStatus)) {
        if (addressType.equals("3") && addressStatus.equalsIgnoreCase("n")) {
          preferredContactTypeId = EMAIL;
        } else if ((addressType.equals("1") || addressType.equals("2")) && addressStatus.equalsIgnoreCase("n")) {
          preferredContactTypeId = MAIL;
        }
      }

      if (Objects.nonNull(preferredContactTypeId)) {
        personal.setPreferredContactTypeId(preferredContactTypeId);
      } else if (Objects.nonNull(defaults.getPreferredContactType())) {
        personal.setPreferredContactTypeId(defaults.getPreferredContactType());
      }
    }
  }

  private void setExternalSystemId(Userdata userdata) {
    if (Objects.nonNull(institutionId)) {
      userdata.setExternalSystemId(institutionId.replaceAll("[()]", ""));
    } else {
      userdata.setExternalSystemId(schema + "_" + patronId);
    }
  }

  private void setActive(Userdata userdata) {
    long charges = Long.parseLong(currentCharges);

    if (Objects.nonNull(activeDate)) {
      if (Objects.nonNull(currentCharges) && charges > 0) {
        userdata.setActive(true);
      } else {
        if (Objects.nonNull(expireDate)) {
          try {
            userdata.setExpirationDate(DATE_FORMAT.parse(expireDate));
            userdata.setActive(!(new Date()).after(userdata.getExpirationDate()));
          } catch (ParseException e) {
            // assume unexpired on invalid date.
            userdata.setActive(true);
          }
        }
      }
    } else {
      userdata.setActive(true);
    }
  }

  private void setBarcode(Userdata userdata) {
    if (Objects.nonNull(barcode)) {
      userdata.setBarcode(barcode);
    }
  }

  private void setUsername(Userdata userdata) {
    if (Objects.nonNull(username)) {
      userdata.setUsername(username);
    }
  }

}