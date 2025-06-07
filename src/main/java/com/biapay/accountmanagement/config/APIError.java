package com.biapay.accountmanagement.config;

public enum APIError {
  INVALID_CUSTOMER("Invalid customer"),
  ACCOUNT_NOT_FOUND("Account not found"),
  BENEFICIARY_ACCOUNT_NOT_FOUND("Beneficiary account not found"),
  ACCOUNT_CUSTOMER_NOT_FOUND("Account customer NOT found"),
  ACCOUNT_ALREADY_EXISTS("Account already exists"),
  INVALID_REQUEST_MONEY_IDENTIFIER("Invalid request money identifier"),
  INVALID_MFA_TOKEN("Invalid MFA token"),
  INVALID_BENEFICIARY_IDENTIFIER("Invalid beneficiary id"),
  BENEFICIARY_NOT_FOUND("Beneficiary not found"),
  ACCOUNT_TRANSACTION_LIMIT_EXCEEDED("Account transaction limit exceeded"),
  REQUEST_MONEY_NOT_EXIST("Invalid request money"),
  CANNOT_REQUEST_FROM_YOURSELF("You can't request money from yourself"),
  IN_SUFFICIENT_BALANCE("Transaction failed because of insufficient balance"),
  ACCOUNT_LIMIT_CHECK_FAILED("Account limit check failed"),
  ;

  private final String description;

  APIError(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }
}
