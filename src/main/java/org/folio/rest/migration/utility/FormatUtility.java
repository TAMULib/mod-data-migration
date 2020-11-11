package org.folio.rest.migration.utility;

import java.util.Locale;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

import org.apache.commons.lang3.StringUtils;

public class FormatUtility {

  private FormatUtility() {

  }

  public static String normalizePostalCode(String postalCode) {
    if (StringUtils.isNotEmpty(postalCode)) {
      // simple fix for trailing hyphens
      postalCode = StringUtils.removeEnd(postalCode, "-");
      // handle postal codes from DivITs patron database
      if (postalCode.length() == 9 && !postalCode.contains("-")) {
        postalCode = String.format("%s-%s", postalCode.substring(0, 5), postalCode.substring(5));
      }
    }
    return postalCode;
  }

  public static String normalizePhoneNumber(String phoneNumber) {
    if (phoneNumber.startsWith("#") || phoneNumber.startsWith("*")) {
      return phoneNumber;
    }
    PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
    if (StringUtils.isNotEmpty(phoneNumber)) {
      String country = Locale.getDefault().getCountry();
      try {
        PhoneNumber numberProto = phoneUtil.parseAndKeepRawInput(phoneNumber, country);
        System.out.println("before " + phoneNumber);
        if (numberProto.getCountryCode() == phoneUtil.getCountryCodeForRegion(country)) {
          phoneNumber = phoneUtil.format(numberProto, PhoneNumberFormat.NATIONAL);
        } else {
          System.out.println("international ******************************************************");
          phoneNumber = phoneUtil.format(numberProto, PhoneNumberFormat.INTERNATIONAL);
        }
        System.out.println("after " + phoneNumber);
      } catch (NumberParseException e) {
        System.err.println(phoneNumber + " could not be parsed: " + e.toString());
      }
    }
    return phoneNumber;
  }

}
