package org.folio.rest.migration.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;
import org.folio.rest.jaxrs.model.Address;
import org.folio.rest.jaxrs.model.Personal;
import org.folio.rest.jaxrs.model.Userdata;

public class UserRecord extends AbstractUserRecord {

  private static final String PATRON = "patron";

  private static final String EMAIL = "email";
  private static final String MAIL = "mail";
  private static final String TEXT = "text";

  private static final String PHONE_PRIMARY = "Primary";
  private static final String PHONE_MOBILE = "Mobile";

  private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("YYYYMMDD");
  {
    // TODO: the timezone may need to be changed.
    DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
  }

  private final String referenceId;
  private final String patronId;
  private final String externalSystemId;
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
  private String groupcode;
  private String preferredContactTypeId;
  private String username;

  private List<UserAddressRecord> userAddressRecords;

  public UserRecord(String referenceId, String patronId, String externalSystemId, String lastName, String firstName, String middleName, String activeDate, String expireDate, String smsNumber, String currentCharges) {
    this.referenceId = referenceId;
    this.patronId = patronId;
    this.externalSystemId = externalSystemId;
    this.lastName = lastName;
    this.firstName = firstName;
    this.middleName = middleName;
    this.activeDate = activeDate;
    this.expireDate = expireDate;
    this.smsNumber = smsNumber;
    this.currentCharges = currentCharges;
  }

  public String getReferenceId() {
    return referenceId;
  }

  public String getPatronId() {
    return patronId;
  }

  public String getExternalSystemId() {
    return externalSystemId;
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

  public String getGroupcode() {
    return groupcode;
  }

  public void setGroupcode(String groupcode) {
    this.groupcode = groupcode;
  }

  public String getPreferredContactTypeId() {
    return preferredContactTypeId;
  }

  public void setPreferredContactTypeId(String preferredContactTypeId) {
    this.preferredContactTypeId = preferredContactTypeId;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public List<UserAddressRecord> getUserAddressRecords() {
    return userAddressRecords;
  }

  public void setUserAddressRecords(List<UserAddressRecord> userAddressRecords) {
    this.userAddressRecords = userAddressRecords;
  }

  public Userdata toUserdata(String patronGroup) {
    final Userdata userdata = new Userdata();
    final Personal personal = new Personal();

    setLastName(personal);
    setFirstName(personal);
    setMiddleName(personal);
    setPreferredContactTypeId(personal);

    setAddresses(personal);

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

  private void setAddresses(Personal personal) {
    List<Address> addresses = new ArrayList<>();

    boolean permanentStatusNormal = false;
    boolean temporaryStatusNormal = false;

    List<String> phoneNumbers = new ArrayList<>();
    List<String> phoneTypes = new ArrayList<>();

    for (UserAddressRecord userAddressRecord : userAddressRecords) {

      userAddressRecord.setMaps(maps);
      userAddressRecord.setDefaults(defaults);

      if (userAddressRecord.isEmail()) {
        personal.setEmail(userAddressRecord.toEmail());
      } else {
        if (userAddressRecord.hasPhoneNumber()) {
          // phone type is stored as phone description.
          phoneNumbers.add(userAddressRecord.getPhoneNumber());
          phoneTypes.add(userAddressRecord.getPhoneDescription());
        } else {
          phoneNumbers.add(StringUtils.EMPTY);
          phoneTypes.add(StringUtils.EMPTY);
        }

        if (userAddressRecord.isPrimary()) {
          if (userAddressRecord.isNormal()) {
            permanentStatusNormal = true;
          }
        } else if (userAddressRecord.isTemporary()) {
          if (userAddressRecord.isNormal()) {
            temporaryStatusNormal = true;
          }
        }
        addresses.add(userAddressRecord.toAddress());
      }

    }

    if (permanentStatusNormal && temporaryStatusNormal) {
      addresses.forEach(userAddressRecord -> {
        if (userAddressRecord.getPrimaryAddress()) {
          userAddressRecord.setPrimaryAddress(defaults.getPrimaryAddress());
        } else {
          userAddressRecord.setPrimaryAddress(!defaults.getPrimaryAddress());
        }
      });
    }

    for (int i = 0; i < addresses.size(); i++) {
      Address address = addresses.get(i);
      if (address.getPrimaryAddress()) {
        if (phoneTypes.get(i).equalsIgnoreCase(PHONE_PRIMARY)) {
          personal.setPhone(phoneNumbers.get(i));
        } else if (phoneTypes.get(i).equalsIgnoreCase(PHONE_MOBILE)) {
          personal.setMobilePhone(phoneNumbers.get(i));
        }
      }
    }
  }

  private void setExternalSystemId(Userdata userdata) {
    if (Objects.nonNull(externalSystemId)) {
      userdata.setExternalSystemId(externalSystemId);  
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