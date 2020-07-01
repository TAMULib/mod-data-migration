package org.folio.rest.migration.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.folio.rest.jaxrs.model.acq_models.acquisitions_unit.schemas.Metadata;
import org.folio.rest.jaxrs.model.acq_models.mod_orgs.schemas.Address;
import org.folio.rest.jaxrs.model.acq_models.mod_orgs.schemas.Contact;
import org.folio.rest.jaxrs.model.acq_models.mod_orgs.schemas.Email;
import org.folio.rest.jaxrs.model.acq_models.mod_orgs.schemas.Url;
import org.folio.rest.migration.model.request.VendorDefaults;
import org.folio.rest.migration.model.request.VendorMaps;

public class VendorAddressRecord {

  private final String id;
  private final String addressLine1;
  private final String addressLine1Full;
  private final String addressLine2;
  private final String city;
  private final String contactName;
  private final String contactTitle;
  private final String country;
  private final String emailAddress;
  private final String stateProvince;
  private final String zipPostal;

  private final List<String> categories;

  private String createdByUserId;
  private Date createdDate;

  private VendorMaps maps;
  private VendorDefaults defaults;

  public VendorAddressRecord(String id, String addressLine1, String addressLine1Full, String addressLine2, String city, String contactName, String contactTitle, String country, String emailAddress, String stateProvince, String zipPostal, List<String> categories) {
    this.id = id;
    this.addressLine1 = addressLine1;
    this.addressLine1Full = addressLine1Full;
    this.addressLine2 = addressLine2;
    this.city = city;
    this.contactName = contactName;
    this.contactTitle = contactTitle;
    this.country = country;
    this.emailAddress = emailAddress;
    this.stateProvince = stateProvince;
    this.zipPostal = zipPostal;

    this.categories = categories;
  }

  public String getId() {
    return id;
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

  public String getZipPostal() {
    return zipPostal;
  }

  public List<String> getCategories() {
    return categories;
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

  public VendorMaps getMaps() {
    return maps;
  }

  public void setMaps(VendorMaps vendorMaps) {
    this.maps = vendorMaps;
  }

  public VendorDefaults getDefaults() {
    return defaults;
  }

  public void setDefaults(VendorDefaults vendorDefaults) {
    this.defaults = vendorDefaults;
  }

  public boolean isAddress() {
    return Objects.isNull(contactName) && !Objects.isNull(city) && !Objects.isNull(country);
  }

  public boolean isContact() {
    return !Objects.isNull(contactName);
  }

  public boolean isEmail() {
    if (Objects.isNull(city) || Objects.isNull(country)) {
      return emailAddress.equalsIgnoreCase("y") || addressLine1.contains("@");
    }

    return false;
  }

  public boolean isUrl() {
    if (Objects.isNull(city) || Objects.isNull(country)) {
      return addressLine1.matches("^(http|www)");
    }

    return false;
  }

  public Address toAddress() {
    final Address address = new Address();

    address.setId(id);
    address.setAddressLine1(addressLine1Full);
    address.setCity(city);
    address.setStateRegion(stateProvince);
    address.setZipCode(zipPostal);

    address.setCategories(categories);

    setAddressLine2(address);
    setCountry(address);
    setLanguage(address);
    setMetadata(address);

    return address;
  }

  public Contact toContact() {
    final Contact contact = new Contact();

    contact.setId(UUID.randomUUID().toString());
    contact.setFirstName(contactName);
    contact.setLastName(" ");

    contact.setCategories(categories);

    setAddress(contact);
    setEmail(contact);
    setMetadata(contact);

    return contact;
  }

  public Email toEmail() {
    final Email email = new Email();

    email.setId(id);
    email.setValue(addressLine1);

    email.setCategories(categories);

    setDescription(email);
    setMetadata(email);

    return email;
  }

  public Url toUrl() {
    final Url url = new Url();

    url.setId(id);

    url.setCategories(categories);

    setDescription(url);
    setValue(url);
    setMetadata(url);

    return url;
  }

  private void setAddressLine2(Address address) {
    if (!Objects.isNull(addressLine2) && addressLine2.matches("\\S")) {
      address.setAddressLine2(addressLine2);
    }
  }

  private void setCountry(Address address) {
    // TODO: get the valid country codes and map them here, if not in map then do not assign.
    if (Objects.isNull(country)) {
      if (!Objects.isNull(defaults.getCountry())) {
        address.setCountry(defaults.getCountry());
      }
    } else {
      address.setCountry(country);
    }
  }

  private void setLanguage(Address address) {
    String language = defaults.getLanguage();
    if (!Objects.isNull(language)) {
      address.setLanguage(defaults.getLanguage());
    }
  }

  private void setAddress(Contact contact) {
    if (!addressLine1.contains("@")) {
      List<Address> addresses = new ArrayList<>();
      addresses.add(toAddress());
      contact.setAddresses(addresses);
    }
  }

  private void setEmail(Contact contact) {
    if (addressLine1.contains("@")) {
      List<Email> emails = new ArrayList<>();
      emails.add(toEmail());
      contact.setEmails(emails);

      String notes = "";
      if (!Objects.isNull(addressLine2)) {
        notes += addressLine2;
      }

      if (!Objects.isNull(contactTitle)) {
        notes += contactTitle;
      }

      if (!notes.isEmpty()) {
        contact.setNotes(notes);
      }
    }
  }

  private void setDescription(Email email) {
    if (Objects.isNull(addressLine2)) {
      String description = "";

      if (!Objects.isNull(email.getDescription())) {
        description = email.getDescription() + " ";
      }
      description += addressLine2;

      email.setDescription(description);
    }
  }

  private void setDescription(Url url) {
    if (Objects.isNull(addressLine2)) {
      String description = "";

      if (!Objects.isNull(url.getDescription())) {
        description = url.getDescription() + " ";
      }
      description += addressLine2;

      url.setDescription(description);
    }
  }

  private void setValue(Url url) {
    if (addressLine1.matches("^http")) {
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