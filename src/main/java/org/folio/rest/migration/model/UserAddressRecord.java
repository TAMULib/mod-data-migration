package org.folio.rest.migration.model;

public class UserAddressRecord {

  private final Integer addressType;
  private final String addressDescription;
  private final String addressStatus;
  private final String addressLine1;
  private final String addressLine2;
  private final String city;
  private final String country;
  private final String phoneNumber;
  private final String phoneDescription;
  private final String stateProvince;
  private final String zipPostal;

  public UserAddressRecord(Integer addressType, String addressDescription, String addressStatus, String addressLine1,
      String addressLine2, String city, String country, String phoneNumber, String phoneDescription,
      String stateProvince, String zipPostal) {
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

  public Integer getAddressType() {
    return addressType;
  }

  public String getAddressDescription() {
    return addressDescription;
  }

  public String getAddressStatus() {
    return addressStatus;
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

}
