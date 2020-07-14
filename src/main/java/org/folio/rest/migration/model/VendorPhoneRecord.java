package org.folio.rest.migration.model;

import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.folio.rest.jaxrs.model.acq_models.mod_orgs.schemas.PhoneNumber;
import org.folio.rest.jaxrs.model.acq_models.mod_orgs.schemas.PhoneNumber.Type;
import org.folio.rest.migration.model.request.vendor.VendorDefaults;

public class VendorPhoneRecord {

  private final String addressId;
  private final String number;
  private final String type;

  private List<String> categories;

  public VendorPhoneRecord(String addressId, String number, String type, List<String> categories) {
    this.addressId = addressId;
    this.number = number;
    this.type = type;
    this.categories = categories;
  }

  public String getAddressId() {
    return addressId;
  }

  public String getNumber() {
    return number;
  }

  public String getType() {
    return type;
  }

  public List<String> getCategories() {
    return categories;
  }

  public PhoneNumber toPhoneNumber(VendorDefaults defaults) {
    final PhoneNumber phoneNumber = new PhoneNumber();

    phoneNumber.setPhoneNumber(number);
    
    phoneNumber.setCategories(categories);

    setLanguage(phoneNumber, defaults);
    setTypeAndPrimary(phoneNumber, defaults);

    return phoneNumber;
  }

  private void setLanguage(PhoneNumber phoneNumber, VendorDefaults defaults) {
    String language = defaults.getLanguage();
    if (Objects.nonNull(language)) {
      phoneNumber.setLanguage(defaults.getLanguage());
    }
  }

  private void setTypeAndPrimary(PhoneNumber phoneNumber, VendorDefaults defaults) {
    String match = StringUtils.EMPTY;

    if (Objects.isNull(type)) {
      if (Objects.nonNull(defaults.getPhoneType())) {
        match = defaults.getPhoneType();
      }
    } else {
      if (type.equals("0")) {
        match = type;
      } else if (type.equals("1")) {
        match = Type.MOBILE.toString();
      } else if (type.equals("2")) {
        match = Type.FAX.toString();
      } else {
        match = Type.OTHER.toString();
      }
    }

    if (match.equals("0")) {
      phoneNumber.setIsPrimary(true);
    } else {
      phoneNumber.setIsPrimary(false);

      if (match.equalsIgnoreCase(Type.FAX.toString())) {
        phoneNumber.setType(Type.FAX);
      } else if (match.equalsIgnoreCase(Type.MOBILE.toString())) {
        phoneNumber.setType(Type.MOBILE);
      } else if (match.equalsIgnoreCase(Type.OTHER.toString())) {
        phoneNumber.setType(Type.OTHER);
      }
    }
  }

}