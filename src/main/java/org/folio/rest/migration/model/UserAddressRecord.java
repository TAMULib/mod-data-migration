package org.folio.rest.migration.model;

import java.util.Objects;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.folio.rest.jaxrs.model.users.Address;
import org.folio.rest.migration.model.request.user.UserDefaults;

public class UserAddressRecord {

  private final String addressTypeId;
  private final String addressStatus;
  private final String addressType;
  private final String addressLine1;
  private final String addressLine2;
  private final String city;
  private final String country;
  private final String phoneNumber;
  private final String phoneDescription;
  private final String stateProvince;
  private final String zipPostal;

  public UserAddressRecord(String addressTypeId, String addressStatus, String addressType, String addressLine1, String addressLine2, String city, String country, String phoneNumber, String phoneDescription, String stateProvince, String zipPostal) {
    this.addressTypeId = addressTypeId;
    this.addressStatus = addressStatus;
    this.addressType = addressType;
    this.addressLine1 = addressLine1;
    this.addressLine2 = addressLine2;
    this.city = city;
    this.country = country;
    this.phoneNumber = phoneNumber;
    this.phoneDescription = phoneDescription;
    this.stateProvince = stateProvince;
    this.zipPostal = zipPostal;
  }

  public String getAddressTypeId() {
    return addressTypeId;
  }

  public String getAddressStatus() {
    return addressStatus;
  }

  public String getAddressType() {
    return addressType;
  }

  public String getAddressLine1() {
    return addressLine1;
  }

  public String getAddressLine2() {
    return addressLine2;
  }

  public String getCity() {
    return city;
  }

  public String getCountry() {
    return country;
  }

  public String getPhoneNumber() {
    return phoneNumber;
  }

  public String getPhoneDescription() {
    return phoneDescription;
  }

  public String getStateProvince() {
    return stateProvince;
  }

  public String getZipPostal() {
    return zipPostal;
  }

  public boolean isPrimary() {
    return Objects.nonNull(addressType) && addressType.equals("1");
  }

  public boolean isTemporary() {
    return Objects.nonNull(addressType) && addressType.equals("2");
  }

  public boolean isEmail() {
    return Objects.nonNull(addressType) && addressType.equals("3");
  }

  public boolean isNormal() {
    return Objects.nonNull(addressStatus) && addressStatus.equalsIgnoreCase("n");
  }

  public boolean hasPhoneNumber() {
    return !isEmail() && Objects.nonNull(phoneNumber);
  }

  public Address toAddress() {
    final Address address = new Address();

    address.setAddressTypeId(addressTypeId);

    setAddressLine1(address);
    setAddressLine2(address);
    setCity(address);
    setCountry(address);
    setPostalCode(address);
    setPrimaryAddress(address);
    setRegion(address);

    return address;
  }

  public String toEmail(UserDefaults defaults) {
    if (Objects.nonNull(defaults.getTemporaryEmail())) {
      if (defaults.getTemporaryEmail().isEmpty()) {
        return addressLine1;
      }

      return defaults.getTemporaryEmail();
    }

    return StringUtils.EMPTY;
  }

  private void setAddressLine1(Address address) {
    if (Objects.nonNull(addressLine1)) {
      address.setAddressLine1(addressLine1);
    }
  }

  private void setAddressLine2(Address address) {
    if (Objects.nonNull(addressLine2)) {
      String updatedAddressLine2 = addressLine2.replaceAll("\\s+", StringUtils.SPACE);
      updatedAddressLine2 = updatedAddressLine2.replaceAll("\\s$", StringUtils.EMPTY);

      address.setAddressLine2(updatedAddressLine2);
    }
  }

  private void setCity(Address address) {
    if (Objects.nonNull(city)) {
      address.setCity(city);
    }
  }

  private void setCountry(Address address) {
    // country is currently not being set.
  }

  private void setPostalCode(Address address) {
    if (Objects.nonNull(zipPostal)) {
      address.setPostalCode(zipPostal);
    }
  }

  private void setPrimaryAddress(Address address) {
    if (Objects.nonNull(addressStatus) && addressStatus.equalsIgnoreCase("n")) {
      address.setPrimaryAddress(true);
    } else {
      address.setPrimaryAddress(false);
    }
  }

  private void setRegion(Address address) {
    if (Objects.nonNull(stateProvince)) {
      address.setRegion(stateProvince);
    }
  }

}
