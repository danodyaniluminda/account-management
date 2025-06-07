package com.biapay.accountmanagement.dto;

import com.biapay.core.constant.enums.AccountStatus;
import com.biapay.core.model.UserType;
import com.biapay.core.model.enums.AccountType;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class AccountRes {

  private String accountUserId;

  private UserType accountUserType;

  private String countryCode;

  private String currencyCode;

  private BigDecimal balance;

  private AccountStatus accountStatus;

  private AccountType accountType;
}
