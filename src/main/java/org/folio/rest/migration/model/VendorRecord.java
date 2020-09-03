package org.folio.rest.migration.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.folio.rest.jaxrs.model.acq_models.acquisitions_unit.schemas.Metadata;
import org.folio.rest.jaxrs.model.acq_models.mod_orgs.schemas.Address;
import org.folio.rest.jaxrs.model.acq_models.mod_orgs.schemas.Email;
import org.folio.rest.jaxrs.model.acq_models.mod_orgs.schemas.Organization;
import org.folio.rest.jaxrs.model.acq_models.mod_orgs.schemas.Url;
import org.folio.rest.migration.model.request.vendor.VendorDefaults;

public class VendorRecord {

  private final String referenceId;
  private final String vendorId;
  private final String code;
  private final String type;
  private final String name;
  private final String taxId;
  private final String defaultCurrency;

  private final Integer claimingInterval;

  private List<VendorAccountRecord> vendorAccountRecords;
  private List<VendorAddressRecord> vendorAddresses;
  private List<VendorPhoneRecord> vendorPhoneNumbers;
  private List<VendorAliasRecord> vendorAliases;

  private List<Address> addresses;
  private List<String> contacts;
  private List<Email> emails;
  private List<Url> urls;

  private String vendorNotes;

  private String createdByUserId;
  private Date createdDate;

  public VendorRecord(String referenceId, String vendorId, String code, String type, String name, String taxId, String defaultCurrency, Integer claimingInterval) {
    this.referenceId = referenceId;
    this.vendorId = vendorId;
    this.code = code;
    this.type = type;
    this.name = name;
    this.taxId = taxId;
    this.defaultCurrency = defaultCurrency;
    this.claimingInterval = claimingInterval;
    vendorAccountRecords = new ArrayList<>();
    vendorAddresses = new ArrayList<>();
    vendorPhoneNumbers = new ArrayList<>();
    vendorAliases = new ArrayList<>();
    addresses = new ArrayList<>();
    contacts = new ArrayList<>();
    emails = new ArrayList<>();
    urls = new ArrayList<>();
  }
  
  public String getReferenceId() {
    return referenceId;
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

  public String getTaxId() {
    return taxId;
  }

  public String getDefaultCurrency() {
    return defaultCurrency;
  }

  public Integer getClaimingInterval() {
    return claimingInterval;
  }

  public List<VendorAccountRecord> getVendorAccountRecords() {
    return vendorAccountRecords;
  }

  public void setVendorAccountRecords(List<VendorAccountRecord> vendorAccountRecords) {
    this.vendorAccountRecords = vendorAccountRecords;
  }

  public List<VendorAddressRecord> getVendorAddresses() {
    return vendorAddresses;
  }

  public void setVendorAddresses(List<VendorAddressRecord> vendorAddresses) {
    this.vendorAddresses = vendorAddresses;
  }

  public List<VendorPhoneRecord> getVendorPhoneNumbers() {
    return vendorPhoneNumbers;
  }

  public void setVendorPhoneNumbers(List<VendorPhoneRecord> vendorPhoneNumbers) {
    this.vendorPhoneNumbers = vendorPhoneNumbers;
  }

  public List<VendorAliasRecord> getVendorAliases() {
    return vendorAliases;
  }

  public void setVendorAliases(List<VendorAliasRecord> vendorAliases) {
    this.vendorAliases = vendorAliases;
  }

  public List<Address> getAddresses() {
    return addresses;
  }

  public void setAddresses(List<Address> addresses) {
    this.addresses = addresses;
  }

  public List<String> getContacts() {
    return contacts;
  }

  public void setContacts(List<String> contacts) {
    this.contacts = contacts;
  }

  public List<Email> getEmails() {
    return emails;
  }

  public void setEmails(List<Email> emails) {
    this.emails = emails;
  }

  public List<Url> getUrls() {
    return urls;
  }

  public void setUrls(List<Url> urls) {
    this.urls = urls;
  }

  public String getVendorNotes() {
    return vendorNotes;
  }

  public void setVendorNotes(String vendorNotes) {
    this.vendorNotes = vendorNotes;
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

  public Organization toOrganization(VendorDefaults defaults) {
    final Organization organization = new Organization();

    organization.setId(referenceId);
    organization.setIsVendor(true);
    organization.setName(name);
    if (StringUtils.isNotEmpty(taxId)) {
      organization.setTaxId(taxId);
    }
    if (Objects.nonNull(claimingInterval)) {
      organization.setClaimingInterval(claimingInterval);
    }

    organization.setAccounts(vendorAccountRecords.stream().map(vendorAccount -> vendorAccount.toAccount(defaults)).collect(Collectors.toList()));

    organization.setPhoneNumbers(vendorPhoneNumbers.stream().map(vendorPhoneNumber -> vendorPhoneNumber.toPhoneNumber(defaults)).collect(Collectors.toList()));

    organization.setAddresses(addresses);
    organization.setContacts(contacts);
    organization.setEmails(emails);
    organization.setUrls(urls);

    organization.setAliases(vendorAliases.stream().map(vendorAlias -> vendorAlias.toAlias()).collect(Collectors.toList()));

    setCode(organization);
    setCurrencies(organization);
    setDescription(organization);
    setStatus(organization, defaults);
    setLanguage(organization, defaults);
    setSanCode(organization);
    setMetadata(organization);

    return organization;
  }

  private void setCode(Organization organization) {
    // vendor codes may not contain embedded blanks.
    organization.setCode(code.replaceAll("/ /", StringUtils.EMPTY));
  }

  private void setCurrencies(Organization organization) {
    List<String> currencies = new ArrayList<>();
    if (StringUtils.isNotEmpty(defaultCurrency)) {
      currencies.add(defaultCurrency);
    }

    organization.setVendorCurrencies(currencies);
  }

  private void setDescription(Organization organization) {
    String description = type;

    if (Objects.nonNull(vendorNotes)) {
      if (StringUtils.isEmpty(description)) {
        description = vendorNotes;
      } else {
        description += StringUtils.SPACE + vendorNotes;
      }
    }

    organization.setDescription(description);
  }

  private void setStatus(Organization organization, VendorDefaults defaults) {
    String match = StringUtils.EMPTY;

    if (Objects.nonNull(defaults.getStatus())) {
      match = defaults.getStatus();
    }

    if (match.equalsIgnoreCase(Organization.Status.ACTIVE.name())) {
      organization.setStatus(Organization.Status.ACTIVE);
    } else if (match.equalsIgnoreCase(Organization.Status.PENDING.name())) {
      organization.setStatus(Organization.Status.PENDING);
    } else if (match.equalsIgnoreCase(Organization.Status.INACTIVE.name())) {
      organization.setStatus(Organization.Status.INACTIVE);
    } else {
      organization.setStatus(Organization.Status.ACTIVE);
    }
  }

  private void setLanguage(Organization organization, VendorDefaults defaults) {
    String language = defaults.getLanguage();
    if (Objects.nonNull(language)) {
      organization.setLanguage(defaults.getLanguage());
    }
  }

  private void setSanCode(Organization organization) {
    for (VendorAddressRecord vendorAddress : vendorAddresses) {
      if (Objects.nonNull(vendorAddress.getStdAddressNumber())) {
        organization.setSanCode(vendorAddress.getStdAddressNumber());
        break;
      }
    }
  }

  private void setMetadata(Organization organization) {
    Metadata metadata = new Metadata();
    metadata.setCreatedByUserId(createdByUserId);
    metadata.setCreatedDate(createdDate);

    organization.setMetadata(metadata);
  }

}
