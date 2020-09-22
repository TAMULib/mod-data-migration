package org.folio.rest.migration.utility;

import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DateUtility {

  private DateUtility() {

  }

  public static Date toDate(String value) {
    Date date = Date.from(Instant.parse(value));
    return accountForDaylightTime(date);
  }

  public static Date accountForDaylightTime(Date date) {
    TimeZone tz = TimeZone.getDefault();
    if (tz.inDaylightTime(date)) {
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(date);
      calendar.add(Calendar.HOUR_OF_DAY, 1);
      return calendar.getTime();
    }
    return date;
  }

}
