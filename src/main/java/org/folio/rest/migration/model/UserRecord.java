package org.folio.rest.migration.model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.folio.rest.jaxrs.model.userimport.raml_util.schemas.tagged_record_example.Metadata;
import org.folio.rest.jaxrs.model.userimport.schemas.Address;
import org.folio.rest.jaxrs.model.userimport.schemas.Personal;
import org.folio.rest.jaxrs.model.userimport.schemas.Userdataimport;
import org.folio.rest.migration.model.request.user.UserDefaults;
import org.folio.rest.migration.model.request.user.UserMaps;

public class UserRecord {

  private static final String PATRON = "patron";

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

  public UserRecord(String referenceId, String patronId, String externalSystemId, String lastName, String firstName,
      String middleName, String expireDate, String smsNumber, String currentCharges) {
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

  public Userdataimport toUserdataimport(UserDefaults defaults, UserMaps maps) {
    final Personal personal = new Personal();

    personal.setLastName(lastName);
    personal.setFirstName(firstName);
    personal.setMiddleName(middleName);

    Map<Integer, Address> orderedAddresses = new HashMap<>();

    for (UserAddressRecord userAddressRecord : userAddressRecords) {
      int type = userAddressRecord.getAddressType();

      Integer index = type - 1;

      if (type == 3) {
        personal.setEmail(userAddressRecord.getAddressLine1());
      } else {
        Address address = new Address();

        address.setAddressTypeId(userAddressRecord.getAddressDescription());

        address.setPrimaryAddress(false);

        if (StringUtils.isNotEmpty(userAddressRecord.getAddressLine1())) {
          address.setAddressLine1(userAddressRecord.getAddressLine1());
        }

        if (StringUtils.isNotEmpty(userAddressRecord.getAddressLine2())) {
          String addressLine2 = userAddressRecord.getAddressLine2().replaceAll("\\s+", StringUtils.SPACE);
          addressLine2 = addressLine2.replaceAll("\\s$", StringUtils.EMPTY);
          address.setAddressLine2(addressLine2);
        }

        if (StringUtils.isNotEmpty(userAddressRecord.getCity())) {
          address.setCity(userAddressRecord.getCity());
        }

        if (StringUtils.isNotEmpty(userAddressRecord.getStateProvince())) {
          address.setRegion(userAddressRecord.getStateProvince());
        }

        if (StringUtils.isNotEmpty(userAddressRecord.getZipPostal())) {
          address.setPostalCode(userAddressRecord.getZipPostal());
        }

        if (StringUtils.isNotEmpty(userAddressRecord.getPhoneNumber())) {

          if (userAddressRecord.getPhoneDescription().equalsIgnoreCase("Primary")) {
            personal.setPhone(userAddressRecord.getPhoneNumber());
          } else if (userAddressRecord.getPhoneDescription().equalsIgnoreCase("Mobile")) {
            personal.setMobilePhone(userAddressRecord.getPhoneNumber());
          }
        }

        orderedAddresses.put(index, address);
      }

    }

    if (orderedAddresses.containsKey(1) && (groupcode.equalsIgnoreCase("grad") || groupcode.equalsIgnoreCase("ungr"))) {
      orderedAddresses.get(1).setPrimaryAddress(true);
    } else {
      orderedAddresses.get(0).setPrimaryAddress(true);
    }

    personal.setAddresses(new ArrayList<>(orderedAddresses.values()));


    // NOTE: always setting preferred contact type to email 002

    // if (!defined $users_voyager[$user_index]{'personal'}{'preferredContactTypeId'}) {
    //   if (defined $email_status && $email_status =~ /N/) {
    //     $users_voyager[$user_index]{'personal'}{'preferredContactTypeId'} = 'email';
    //   }
    //   elsif ($perm_status =~ /N/ or $temp_status =~ /N/) {
    //     $users_voyager[$user_index]{'personal'}{'preferredContactTypeId'} = 'mail';
    //   }
    //   else {
    //     $users_voyager[$user_index]{'personal'}{'preferredContactTypeId'} = $default_preferred_contact_type;
    //   }
    // }
    personal.setPreferredContactTypeId(defaults.getPreferredContactType());

    // NOTE: if no email on user, setting to default email
    if (StringUtils.isEmpty(personal.getEmail())) {
      personal.setEmail(defaults.getTemporaryEmail());
    }

    final Userdataimport userdata = new Userdataimport();

    userdata.setId(referenceId);
    userdata.setExternalSystemId(externalSystemId);
    userdata.setUsername(username);
    userdata.setBarcode(barcode);
    userdata.setPatronGroup(groupcode);

    userdata.setType(PATRON);

    userdata.setPersonal(personal);

    // NOTE: always setting user active to expire in 370 days

    // if (defined $hr->{'active_date'}) {
    //   $users_voyager[$user_index]{'expirationDate'}= $hr->{'expire_date'};

    //   my $year = substr($hr->{'active_date'},0,4);
    //   my $month = substr($hr->{'active_date'},4,2);
    //   my $day = substr($hr->{'active_date'},6,2);
    //   my @expire_date = ($year, $month, $day);
    //   my $expired = Delta_Days(@today_date,@expire_date);
    //   if ($hr->{'current_charges'} > 0) {
    //     $users_voyager[$user_index]{'active'} = 'true';
    //     if ($expired < 0) {
    //       $users_voyager[$user_index]{'expirationDate'}= '2021-09-01';
    //     }
    //   }
    //   elsif ($expired > 0) {
    //     $users_voyager[$user_index]{'active'} = 'true';
    //   }	
    //   else {
    //     $users_voyager[$user_index]{'active'} = 'false';
    //   }
    // }
    // else {
    //   $users_voyager[$user_index]{'active'} = 'true';
    // }
    userdata.setActive(true);

    Calendar c = Calendar.getInstance();
    c.setTime(new Date());
    c.add(Calendar.DATE, 370);
    userdata.setExpirationDate(c.getTime());

    Metadata metadata = new Metadata();
    metadata.setCreatedByUserId(createdByUserId);
    metadata.setCreatedDate(createdDate);
    metadata.setUpdatedByUserId(createdByUserId);
    metadata.setUpdatedDate(createdDate);
    userdata.setMetadata(metadata);

    return userdata;
  }

}
