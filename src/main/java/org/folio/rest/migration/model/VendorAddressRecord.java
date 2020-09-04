package org.folio.rest.migration.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.folio.rest.jaxrs.model.acq_models.acquisitions_unit.schemas.Metadata;
import org.folio.rest.jaxrs.model.acq_models.mod_orgs.schemas.Address;
import org.folio.rest.jaxrs.model.acq_models.mod_orgs.schemas.Contact;
import org.folio.rest.jaxrs.model.acq_models.mod_orgs.schemas.Email;
import org.folio.rest.jaxrs.model.acq_models.mod_orgs.schemas.Url;
import org.folio.rest.migration.model.request.vendor.VendorDefaults;
import org.folio.rest.migration.model.request.vendor.VendorMaps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VendorAddressRecord {

  private static final String CLAIM = "claim";
  private static final String ORDER = "order";
  private static final String OTHER = "other";
  private static final String PAYMENT = "payment";
  private static final String RETURN = "return";

  private final Logger log = LoggerFactory.getLogger(this.getClass());

  private final String addressId;
  private final String addressLine1;
  private final String addressLine1Full;
  private final String addressLine2;
  private final String city;
  private final String contactName;
  private final String contactTitle;
  private final String country;
  private final String emailAddress;
  private final String stateProvince;
  private final String stdAddressNumber;
  private final String zipPostal;
  private final String claimAddress;
  private final String orderAddress;
  private final String otherAddress;
  private final String paymentAddress;
  private final String returnAddress;

  private String vendorId;

  private String createdByUserId;
  private Date createdDate;

  public VendorAddressRecord(String addressId, String addressLine1, String addressLine1Full, String addressLine2, String city, String contactName, String contactTitle, String country, String emailAddress, String stateProvince, String stdAddressNumber, String zipPostal, String claimAddress, String orderAddress, String otherAddress, String paymentAddress, String returnAddress) {
    this.addressId = addressId;
    this.addressLine1 = addressLine1;
    this.addressLine1Full = addressLine1Full;
    this.addressLine2 = addressLine2;
    this.city = city;
    this.contactName = contactName;
    this.contactTitle = contactTitle;
    this.country = country;
    this.emailAddress = emailAddress;
    this.stateProvince = stateProvince;
    this.stdAddressNumber = stdAddressNumber;
    this.zipPostal = zipPostal;
    this.claimAddress = claimAddress;
    this.orderAddress = orderAddress;
    this.otherAddress = otherAddress;
    this.paymentAddress = paymentAddress;
    this.returnAddress = returnAddress;
  }

  public String getAddressId() {
    return addressId;
  }

  public String getAddressLine1() {
    return addressLine1;
  }

  public String getAddressLine1Full() {
    return addressLine1Full;
  }

  public String getAddressLine2() {
    return addressLine2;
  }

  public String getCity() {
    return city;
  }

  public String getContactName() {
    return contactName;
  }

  public String getContactTitle() {
    return contactTitle;
  }

  public String getCountry() {
    return country;
  }

  public String getEmailAddress() {
    return emailAddress;
  }

  public String getStateProvince() {
    return stateProvince;
  }

  public String getStdAddressNumber() {
    return stdAddressNumber;
  }

  public String getZipPostal() {
    return zipPostal;
  }

  public String getClaimAddress() {
    return claimAddress;
  }

  public String getOrderAddress() {
    return orderAddress;
  }

  public String getOtherAddress() {
    return otherAddress;
  }

  public String getPaymentAddress() {
    return paymentAddress;
  }

  public String getReturnAddress() {
    return returnAddress;
  }

  public String getVendorId() {
    return vendorId;
  }

  public void setVendorId(String vendorId) {
    this.vendorId = vendorId;
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

  public boolean isAddress() {
    return Objects.isNull(contactName) && (Objects.nonNull(city) || Objects.nonNull(country));
  }

  public boolean isContact() {
    return Objects.nonNull(contactName);
  }

  public boolean isEmail() {
    if (Objects.isNull(city) || Objects.isNull(country)) {
      return emailAddress.equalsIgnoreCase("y") || addressLine1.contains("@");
    }

    return false;
  }

  public boolean isUrl() {
    if (Objects.isNull(city) || Objects.isNull(country)) {
      return addressLine1.toLowerCase().matches("^((https?|ftp)://|(www|ftp)\\.)?[\\w-]+(\\.[\\w-]+)+([/?].*)?$");
    }

    return false;
  }

  public List<String> getCategories(VendorMaps maps) {
    List<String> categories = new ArrayList<>();
    Map<String, String> categoriesMap = maps.getCategories();

    if (StringUtils.isNotEmpty(claimAddress) && claimAddress.equalsIgnoreCase("y")) {
      categories.add(categoriesMap.get(CLAIM));
    }

    if (StringUtils.isNotEmpty(orderAddress) && orderAddress.equalsIgnoreCase("y")) {
      categories.add(categoriesMap.get(ORDER));
    }

    if (StringUtils.isNotEmpty(otherAddress) && otherAddress.equalsIgnoreCase("y")) {
      categories.add(categoriesMap.get(OTHER));
    }

    if (StringUtils.isNotEmpty(paymentAddress) && paymentAddress.equalsIgnoreCase("y")) {
      categories.add(categoriesMap.get(PAYMENT));
    }

    if (StringUtils.isNotEmpty(returnAddress) && returnAddress.equalsIgnoreCase("y")) {
      categories.add(categoriesMap.get(RETURN));
    }

    return categories;
  }

  public Address toAddress(List<String> categories, VendorDefaults defaults, VendorMaps maps) {
    final Address address = new Address();

    address.setId(addressId);
    address.setAddressLine1(addressLine1Full);
    address.setCity(city);
    address.setStateRegion(stateProvince);
    address.setZipCode(zipPostal);

    address.setCategories(categories);

    setAddressLine2(address);
    setCountry(address, defaults, maps);
    setLanguage(address, defaults);
    setMetadata(address);

    return address;
  }

  public Contact toContact(List<String> categories, VendorDefaults defaults, VendorMaps maps) {
    final Contact contact = new Contact();

    contact.setId(UUID.randomUUID().toString());
    contact.setFirstName(contactName);
    contact.setLastName(StringUtils.SPACE);

    contact.setCategories(categories);

    setAddress(contact, categories, defaults, maps);
    setEmail(contact, categories);
    setMetadata(contact);

    return contact;
  }

  public Email toEmail(List<String> categories) {
    final Email email = new Email();

    email.setId(addressId);
    email.setValue(addressLine1);

    email.setCategories(categories);

    setDescription(email);
    setMetadata(email);

    return email;
  }

  public Url toUrl(List<String> categories) {
    final Url url = new Url();

    url.setId(addressId);

    url.setCategories(categories);

    setDescription(url);
    setValue(url);
    setMetadata(url);

    return url;
  }

  private void setAddressLine2(Address address) {
    if (Objects.nonNull(addressLine2) && addressLine2.matches("\\S")) {
      address.setAddressLine2(addressLine2);
    }
  }

  private void setCountry(Address address, VendorDefaults defaults, VendorMaps maps) {
    String match = StringUtils.EMPTY;

    if (Objects.isNull(country)) {
      if (Objects.nonNull(defaults.getCountry())) {
        match = defaults.getCountry();
      }
    } else {
      match = country.toUpperCase();
    }

    Map<String, String> countryCodesMap = maps.getCountryCodes();

    if (countryCodesMap.containsKey(match)) {
      address.setCountry(countryCodesMap.get(match));
    } else if (countryCodesMap.containsValue(match)) {
      address.setCountry(match);
    } else {
      log.error("unknown country code {} for address id {} for vendor id {}", match, addressId, vendorId);
    }
  }

  private void setLanguage(Address address, VendorDefaults defaults) {
    String language = defaults.getLanguage();
    if (Objects.nonNull(language)) {
      address.setLanguage(defaults.getLanguage());
    }
  }

  private void setAddress(Contact contact, List<String> categories, VendorDefaults defaults, VendorMaps maps) {
    if (!addressLine1.contains("@")) {
      List<Address> addresses = new ArrayList<>();
      addresses.add(toAddress(categories, defaults, maps));
      contact.setAddresses(addresses);
    }
  }

  private void setEmail(Contact contact, List<String> categories) {
    if (addressLine1.contains("@")) {
      List<Email> emails = new ArrayList<>();
      emails.add(toEmail(categories));
      contact.setEmails(emails);

      String notes = StringUtils.EMPTY;
      if (Objects.nonNull(addressLine2)) {
        notes += addressLine2;
      }

      if (Objects.nonNull(contactTitle)) {
        notes += contactTitle;
      }

      if (!notes.isEmpty()) {
        contact.setNotes(notes);
      }
    }
  }

  private void setDescription(Email email) {
    if (Objects.isNull(addressLine2)) {
      String description = StringUtils.EMPTY;

      if (Objects.nonNull(email.getDescription())) {
        description = email.getDescription() + StringUtils.SPACE;
      }
      description += addressLine2;

      email.setDescription(description);
    }
  }

  private void setDescription(Url url) {
    if (Objects.isNull(addressLine2)) {
      String description = StringUtils.EMPTY;

      if (Objects.nonNull(url.getDescription())) {
        description = url.getDescription() + StringUtils.SPACE;
      }
      description += addressLine2;

      url.setDescription(description);
    }
  }

  private void setValue(Url url) {
    if (addressLine1.toLowerCase().startsWith("http")) {
      url.setValue(addressLine1);
    } else {
      url.setValue("http://" + addressLine1);
    }
  }

  private void setMetadata(Address address) {
    Metadata metadata = new Metadata();
    metadata.setCreatedByUserId(createdByUserId);
    metadata.setCreatedDate(createdDate);

    address.setMetadata(metadata);
  }

  private void setMetadata(Contact contact) {
    Metadata metadata = new Metadata();
    metadata.setCreatedByUserId(createdByUserId);
    metadata.setCreatedDate(createdDate);

    contact.setMetadata(metadata);
  }

  private void setMetadata(Email email) {
    Metadata metadata = new Metadata();
    metadata.setCreatedByUserId(createdByUserId);
    metadata.setCreatedDate(createdDate);

    email.setMetadata(metadata);
  }

  private void setMetadata(Url url) {
    Metadata metadata = new Metadata();
    metadata.setCreatedByUserId(createdByUserId);
    metadata.setCreatedDate(createdDate);

    url.setMetadata(metadata);
  }

}
