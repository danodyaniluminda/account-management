package com.biapay.accountmanagement.exception;

public class BIARuntimeException extends RuntimeException {

  public BIARuntimeException() {
  }

  public BIARuntimeException(String message) {
    super(message);
  }

  public BIARuntimeException(String message, Throwable cause) {
    super(message, cause);
  }

  public BIARuntimeException(Throwable cause) {
    super(cause);
  }

  public BIARuntimeException(
      String message,
      Throwable cause,
      boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
