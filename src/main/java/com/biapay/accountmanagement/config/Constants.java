package com.biapay.accountmanagement.config;

public class Constants {

  public static final String MOBILE_COUNTRY_CODE = "MOBILE_COUNTRY_CODE";
  public static final String SYSTEM = "System";
  public static final String DEFAULT_CURRENCY_CODE = "XAF";
  public static final String DEFAULT_COUNTRY_CODE = "CM";
  public static final String USER_ID = "USER_ID";
  public static final String USER_TYPE = "USER_TYPE";
  public static final String QR_CODE = "qrCode";
  public static String SPRING_PROFILE_DEVELOPMENT = "dev";
  public static String SPRING_PROFILE_TEST = "test";
  public static String SPRING_PROFILE_PRODUCTION = "prod";

  public enum Roles {
    ROLE_CUSTOMER
  }

  public enum EmailTemplate {
    CUSTOMER_REGISTRATION_PIN,
  }

  public enum SMSTemplate {
    CUSTOMER_REGISTRATION_PIN
  }

  public enum AdminAccountUserId {
    FEE_ACCOUNT,
    TAX_ACCOUNT,
    REWARD_ACCOUNT,
    LOAN_ACCOUNT,
    COMMISSION_ACCOUNT
  }
}
