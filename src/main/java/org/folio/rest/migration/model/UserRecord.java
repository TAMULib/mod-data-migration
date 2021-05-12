package org.folio.rest.migration.model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.folio.rest.jaxrs.model.userimport.raml_util.schemas.tagged_record_example.Metadata;
import org.folio.rest.jaxrs.model.userimport.schemas.Address;
import org.folio.rest.jaxrs.model.userimport.schemas.Personal;
import org.folio.rest.jaxrs.model.userimport.schemas.Userdataimport;
import org.folio.rest.migration.model.request.user.UserDefaults;
import org.folio.rest.migration.model.request.user.UserMaps;
import org.folio.rest.migration.utility.FormatUtility;

public class UserRecord {

  private static final String PATRON = "patron";

  private static final String EMAIL = "Email";
  private static final String MAIL = "Mail";
  private static final String TEXT = "Text message";

  private static final String PHONE_PRIMARY = "Primary";
  private static final String PHONE_MOBILE = "Mobile";

  // private static final String EXPIRED_DATE_FORMAT = "yyyy-MM-dd";

  private final String referenceId;
  private final String patronId;
  private final String externalSystemId;
  private final String lastName;
  private final String firstName;
  private final String middleName;
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

  private String createdByUserId;
  private Date createdDate;

  public UserRecord(String referenceId, String patronId, String externalSystemId, String lastName, String firstName, String middleName, String expireDate, String smsNumber, String currentCharges) {
    this.referenceId = referenceId;
    this.patronId = patronId;
    this.externalSystemId = externalSystemId;
    this.lastName = lastName;
    this.firstName = firstName;
    this.middleName = middleName;
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

  public String getCreatedByUserId() {
    return createdByUserId;
  }

  public void setCreatedByUserId(String createdByUserId) {
    this.createdByUserId = createdByUserId;
  }

  public Date getCreatedDate() {
    return createdDate;
  }

  public void setCreatedDate(Date createdDate) {
    this.createdDate = createdDate;
  }

  public Userdataimport toUserdataimport(String patronGroup, UserDefaults defaults, UserMaps maps) {
    final Userdataimport userdata = new Userdataimport();
    final Personal personal = new Personal();

    setLastName(personal);
    setFirstName(personal);
    setMiddleName(personal);
    setPreferredContactTypeId(personal, defaults, maps);

    setAddresses(personal, defaults);

    userdata.setId(referenceId);
    userdata.setPersonal(personal);
    userdata.setType(PATRON);

    setExternalSystemId(userdata);
    setActive(userdata, defaults);
    setBarcode(userdata);
    setUsername(userdata);

    userdata.setPatronGroup(patronGroup);

    Metadata metadata = new Metadata();
    metadata.setCreatedByUserId(createdByUserId);
    metadata.setCreatedDate(createdDate);
    metadata.setUpdatedByUserId(createdByUserId);
    metadata.setUpdatedDate(createdDate);
    userdata.setMetadata(metadata);

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

  private void setPreferredContactTypeId(Personal personal, UserDefaults defaults, UserMaps maps) {
    // NOTE: always setting preferred contact type to email 002
    personal.setPreferredContactTypeId(defaults.getPreferredContactType());

    // if (Objects.nonNull(smsNumber)) {
    //   preferredContactTypeId = maps.getPreferredContactType().get(TEXT);
    //   personal.setPreferredContactTypeId(preferredContactTypeId);
    // }

    // if (Objects.isNull(preferredContactTypeId)) {
    //   if (Objects.nonNull(addressType) && Objects.nonNull(addressStatus)) {
    //     if (addressType.equals("3") && addressStatus.equalsIgnoreCase("n")) {
    //       preferredContactTypeId = maps.getPreferredContactType().get(EMAIL);
    //     } else if ((addressType.equals("1") || addressType.equals("2")) && addressStatus.equalsIgnoreCase("n")) {
    //       preferredContactTypeId = maps.getPreferredContactType().get(MAIL);
    //     }
    //   }

    //   if (Objects.nonNull(preferredContactTypeId)) {
    //     personal.setPreferredContactTypeId(preferredContactTypeId);
    //   } else if (Objects.nonNull(defaults.getPreferredContactType())) {
    //     personal.setPreferredContactTypeId(defaults.getPreferredContactType());
    //   }
    // }
  }

  private void setAddresses(Personal personal, UserDefaults defaults) {
    List<Address> addresses = new ArrayList<>();

    boolean permanentStatusNormal = false;
    boolean temporaryStatusNormal = false;

    List<String> phoneNumbers = new ArrayList<>();
    List<String> phoneTypes = new ArrayList<>();

    for (UserAddressRecord userAddressRecord : userAddressRecords) {
      if (userAddressRecord.isEmail()) {
        personal.setEmail(userAddressRecord.toEmail(defaults));
      } else {
        if (userAddressRecord.hasPhoneNumber()) {
          // phone type is stored as phone description.
          phoneNumbers.add(FormatUtility.normalizePhoneNumber(userAddressRecord.getPhoneNumber()));
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

    personal.setAddresses(addresses);

    // NOTE: if no email on user, setting to default email
    if (StringUtils.isEmpty(personal.getEmail())) {
      personal.setEmail(defaults.getTemporaryEmail());
    }
  }

  private void setExternalSystemId(Userdataimport userdata) {
    if (Objects.nonNull(externalSystemId)) {
      userdata.setExternalSystemId(externalSystemId);
    }
  }

  private void setActive(Userdataimport userdata, UserDefaults defaults) {
    userdata.setActive(true);

    Calendar c = Calendar.getInstance();
    c.setTime(new Date());
    c.add(Calendar.DATE, 370);
    userdata.setExpirationDate(c.getTime());

    /*
    if (Objects.nonNull(expireDate)) {
      try {
        Date expirationDate = DateUtils.parseDate(expireDate, EXPIRED_DATE_FORMAT);
        userdata.setExpirationDate(expirationDate);
        Date now = new Date();
        boolean hasExpired = now.after(expirationDate);
        if (Objects.nonNull(currentCharges) && Long.parseLong(currentCharges) > 0) {
          userdata.setActive(true);
          if (hasExpired) {
            userdata.setExpirationDate(DateUtils.parseDate(defaults.getExpirationDate(), EXPIRED_DATE_FORMAT));
          }
        } else {
          if (hasExpired) {
            userdata.setActive(false);
          } else {
            userdata.setActive(true);
          }
        }
      } catch (ParseException e) {
        userdata.setActive(true);
      }
    } else {
      userdata.setActive(true);
    }
    */
  }

  private void setBarcode(Userdataimport userdata) {
    if (Objects.nonNull(barcode)) {
      userdata.setBarcode(barcode);
    }
  }

  private void setUsername(Userdataimport userdata) {
    if (Objects.nonNull(username)) {
      userdata.setUsername(username);
    }
  }

}
