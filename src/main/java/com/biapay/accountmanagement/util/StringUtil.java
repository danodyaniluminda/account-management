package com.biapay.accountmanagement.util;

import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;

public class StringUtil {

  /**
   * 10005-00001-05187861101-58
   *
   * @return
   */
  public static String getCustomerIdFromAccountNumber(String accountNumber) {
    String[] array = accountNumber.split("-");
    if (array.length > 3) {
      String thirdStr = array[2];
      if (thirdStr.length() >= 7) {
        return thirdStr.substring(0, 7);
      }
    }

    return null;
  }

//  public static String getTransactionReference() {
//    String stringBuilder = "W"
//      //  + DateUtil.getCurrentDate()
//        + StringUtils.leftPad(String.valueOf(System.nanoTime()), 7, "0");
//
//    return stringBuilder;
//  }

  public static String getTransactionReference() {

    return "W"
        //  + DateUtil.getCurrentDate()
        + System.nanoTime();
  }
}
