package org.folio.rest.migration.model;

import java.util.Objects;
import java.util.UUID;

import org.folio.rest.jaxrs.model.Address;

public class UserAddressRecord extends AbstractUserRecord {

  private final String addressDescription;
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

  public UserAddressRecord(String addressDescription, String addressStatus, String addressType, String addressLine1, String addressLine2, String city, String country, String phoneNumber, String phoneDescription, String stateProvince, String zipPostal) {
    this.addressDescription = addressDescription;
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

  public String getAddressDescription() {
    return addressDescription;
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

    address.setId(UUID.randomUUID().toString());
    address.setAddressTypeId(addressDescription);

    setAddressLine1(address);
    setAddressLine2(address);
    setCity(address);
    setCountry(address);
    setPostalCode(address);
    setPrimaryAddress(address);
    setRegion(address);

    return address;
  }

  public String toEmail() {
    if (Objects.nonNull(defaults.getTemporaryEmail())) {
      if (defaults.getTemporaryEmail().isEmpty()) {
        return addressLine1;
      }

      return defaults.getTemporaryEmail();
    }

    return "";
  }

  private void setAddressLine1(Address address) {
    if (Objects.nonNull(addressLine1)) {
      address.setAddressLine1(addressLine1);
    }
  }

  private void setAddressLine2(Address address) {
    if (Objects.nonNull(addressLine2)) {
      String updatedAddressLine2 = addressLine1.replaceAll("\\s+", " ");
      updatedAddressLine2 = updatedAddressLine2.replaceAll("\\s$", "");

      address.setAddressLine1(updatedAddressLine2);
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