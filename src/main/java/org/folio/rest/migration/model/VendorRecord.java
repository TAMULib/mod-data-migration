package org.folio.rest.migration.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.folio.rest.jaxrs.model.acq_models.acquisitions_unit.schemas.Metadata;
import org.folio.rest.jaxrs.model.acq_models.mod_orgs.schemas.Account;
import org.folio.rest.jaxrs.model.acq_models.mod_orgs.schemas.Address;
import org.folio.rest.jaxrs.model.acq_models.mod_orgs.schemas.Alias;
import org.folio.rest.jaxrs.model.acq_models.mod_orgs.schemas.Email;
import org.folio.rest.jaxrs.model.acq_models.mod_orgs.schemas.Organization;
import org.folio.rest.jaxrs.model.acq_models.mod_orgs.schemas.PhoneNumber;
import org.folio.rest.jaxrs.model.acq_models.mod_orgs.schemas.Url;
import org.folio.rest.migration.model.request.VendorDefaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VendorRecord extends AbstractVendorRecord {

  private final Logger log = LoggerFactory.getLogger(this.getClass());

  private final String vendorId;
  private final String code;
  private final String type;
  private final String name;
  private final String status;
  private final String taxId;
  private final String defaultCurrency;

  private final Integer claimingInterval;

  private String notes;
  private String stdAddressNumber;

  private List<Account> accounts;
  private List<Address> addresses;
  private List<Alias> aliases;
  private List<String> contacts;
  private List<Email> emails;
  private List<PhoneNumber> phoneNumbers;
  private List<Url> urls;

  private String createdByUserId;
  private Date createdDate;

  public VendorRecord(String vendorId, String code, String type, String name, String status, String taxId, String defaultCurrency, Integer claimingInterval) {
    this.vendorId = vendorId;
    this.code = code;
    this.type = type;
    this.name = name;
    this.status = status;
    this.taxId = taxId;
    this.defaultCurrency = defaultCurrency;
    this.claimingInterval = claimingInterval;

    accounts = new ArrayList<Account>();
    addresses = new ArrayList<Address>();
    aliases = new ArrayList<Alias>();
    contacts = new ArrayList<String>();
    emails = new ArrayList<Email>();
    urls = new ArrayList<Url>();
  }

  public String getVendorId() {
    return vendorId;
  }

  public String getCode() {
    return code;
  }

  public String getType() {
    return type;
  }

  public String getName() {
    return name;
  }

  public String getStatus() {
    return status;
  }

  public String getTaxId() {
    return taxId;
  }

  public String getDefaultCurrency() {
    return defaultCurrency;
  }

  public Integer getClaimingInterval() {
    return claimingInterval;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }

  public String getStdAddressNumber() {
    return stdAddressNumber;
  }

  public void setStdAddressNumber(String stdAddressNumber) {
    this.stdAddressNumber = stdAddressNumber;
  }

  public List<Account> getAccounts() {
    return accounts;
  }

  public void addAccount(Account account) {
    accounts.add(account);
  }

  public List<Address> getAddresses() {
    return addresses;
  }

  public void addAddress(Address address) {
    addresses.add(address);
  }

  public List<Alias> getAliases() {
    return aliases;
  }

  public void addAlias(Alias alias) {
    aliases.add(alias);
  }

  public List<String> getContacts() {
    return contacts;
  }

  public void addContact(String contact) {
    contacts.add(contact);
  }

  public List<Email> getEmails() {
    return emails;
  }

  public void addEmail(Email email) {
    emails.add(email);
  }

  public List<PhoneNumber> getPhoneNumbers() {
    return phoneNumbers;
  }

  public void addPhoneNumber(PhoneNumber PhoneNumber) {
    phoneNumbers.add(PhoneNumber);
  }

  public List<Url> getUrls() {
    return urls;
  }

  public void setUrl(List<Url> urls) {
    this.urls = urls;
  }

  public void addUrl(Url url) {
    urls.add(url);
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

  public Organization toOrganization() {
    final Organization organization = new Organization();

    organization.setId(vendorId);
    organization.setIsVendor(true);
    organization.setName(name);
    organization.setTaxId(taxId);
    organization.setClaimingInterval(claimingInterval);
    organization.setAccounts(accounts);
    organization.setAliases(aliases);
    organization.setAddresses(addresses);
    organization.setContacts(contacts);
    organization.setEmails(emails);
    organization.setUrls(urls);
    organization.setAccounts(accounts);

    setCode(organization);
    setCurrencies(organization);
    setDescription(organization);
    setStatus(organization, defaults);
    setLanguage(organization);
    setSanCode(organization);
    setMetadata(organization);

    return organization;
  }

  private void setCode(Organization organization) {
    // vendor codes may not contain embedded blanks.
    organization.setCode(code.replaceAll("/ /", ""));
  }

  private void setCurrencies(Organization organization) {
    List<String> currencies = new ArrayList<>();
    currencies.add(defaultCurrency);

    organization.setVendorCurrencies(currencies);
  }

  private void setDescription(Organization organization) {
    String description = type;

    if (!Objects.isNull(notes)) {
      if (Objects.isNull(description)) {
        description = notes;
      } else {
        description += " " + notes;
      }
    }

    organization.setDescription(description);
  }

  private void setStatus(Organization organization, VendorDefaults defaults) {
    String match = "";

    if (Objects.isNull(status)) {
      if (!Objects.isNull(defaults.getStatus())) {
        match = defaults.getStatus();
      }
    } else {
      match = status;
    }

    if (match.equalsIgnoreCase(Organization.Status.ACTIVE.name())) {
      organization.setStatus(Organization.Status.ACTIVE);
    } else if (match.equalsIgnoreCase(Organization.Status.PENDING.name())) {
      organization.setStatus(Organization.Status.PENDING);
    } else if (match.equalsIgnoreCase(Organization.Status.INACTIVE.name())) {
      organization.setStatus(Organization.Status.INACTIVE);
    } else {
      organization.setStatus(Organization.Status.ACTIVE);
      log.error("unknown status {} for vendor id {}, defaulting to {}", match, Organization.Status.ACTIVE, vendorId);
    }
  }

  private void setLanguage(Organization organization) {
    String language = defaults.getLanguage();
    if (!Objects.isNull(language)) {
      organization.setLanguage(defaults.getLanguage());
    }
  }

  private void setSanCode(Organization organization) {
    if (!Objects.isNull(stdAddressNumber)) {
      organization.setSanCode(stdAddressNumber);
    }
  }

  private void setMetadata(Organization organization) {
    Metadata metadata = new Metadata();
    metadata.setCreatedByUserId(createdByUserId);
    metadata.setCreatedDate(createdDate);

    organization.setMetadata(metadata);
  }

}