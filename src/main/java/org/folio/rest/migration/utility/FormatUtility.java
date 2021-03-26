package org.folio.rest.migration.utility;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FormatUtility {

  private static final Logger log = LoggerFactory.getLogger(FormatUtility.class);

  private static final PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();

  private FormatUtility() {

  }

  public static String normalizePostalCode(String postalCode) {
    if (StringUtils.isNotEmpty(postalCode)) {
      // simple fix for trailing hyphens
      postalCode = StringUtils.removeEnd(postalCode, "-");
      // add hyphen if 9 digit postal code
      if (postalCode.length() == 9 && !postalCode.contains("-")) {
        postalCode = String.format("%s-%s", postalCode.substring(0, 5), postalCode.substring(5));
      }
    }
    return postalCode;
  }

  public static String normalizePhoneNumber(String phoneNumber) {
    if (StringUtils.isNotEmpty(phoneNumber)) {
      if (phoneNumber.startsWith("#") || phoneNumber.startsWith("*")) {
        return phoneNumber;
      }
      try {
        String defaultRegion = "US";
        PhoneNumber numberProto = phoneUtil.parseAndKeepRawInput(phoneNumber, defaultRegion);
        if (numberProto.getCountryCode() == phoneUtil.getCountryCodeForRegion(defaultRegion)) {
          phoneNumber = phoneUtil.format(numberProto, PhoneNumberFormat.NATIONAL);
        } else {
          phoneNumber = phoneUtil.format(numberProto, PhoneNumberFormat.INTERNATIONAL);
        }
      } catch (NumberParseException e) {
        log.warn(phoneNumber + " could not be parsed. " + e.getMessage());
      }
    }
    return phoneNumber;
  }

}
