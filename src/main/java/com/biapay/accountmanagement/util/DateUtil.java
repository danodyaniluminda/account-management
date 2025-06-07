package com.biapay.accountmanagement.util;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateUtil {

  public static final String DATE_FORMAT_YYYYMMDD = "yyyyMMdd";

  public static String getCurrentDate() {
    return LocalDate.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT_YYYYMMDD));
  }

  public static LocalDate from(Date date) {
    return date.toInstant()
        .atZone(ZoneId.systemDefault())
        .toLocalDate();
  }
}
