package com.biapay.accountmanagement.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Builder(toBuilder = true)
public class AccountReportDTO {

  @Builder(toBuilder = true)
  @Getter
  @Setter
  public static class AccountAggregateByUserTypeDto {

    private Double balance;
    private String accountUserType;
  }

  @Builder(toBuilder = true)
  @Getter
  @Setter
  public static class AccountAggregateByAccountTypeDto {

    private Double balance;
    private String accountType;
  }

  @Builder(toBuilder = true)
  @Getter
  @Setter
  public static class AccountAggregateByUserTypeCurrencyDto {

    private Double balance;
    private String accountUserType;
    private String accountType;
    private String currency;
    private String date;
  }

  @Builder(toBuilder = true)
  @Getter
  @Setter
  public static class AccountAggregateByCurrencyDto {

    private Double balance;
    private String currency;
    private String date;
  }

  @Builder(toBuilder = true)
  @Getter
  @Setter
  public static class TopAccountAggregatePerCurrencyDto {

    private Double balance;
    private String currency;
    private String date;
  }
}
