package com.biapay.accountmanagement.exception;

public class BIABadRequestException extends RuntimeException {

  public BIABadRequestException() {
  }

  public BIABadRequestException(String message) {
    super(message);
  }

  public BIABadRequestException(String message, Throwable cause) {
    super(message, cause);
  }

  public BIABadRequestException(Throwable cause) {
    super(cause);
  }

  public BIABadRequestException(
      String message,
      Throwable cause,
      boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
