package com.biapay.accountmanagement.dto;

import com.biapay.core.constant.enums.AccountStatus;
import com.biapay.core.model.UserType;
import com.biapay.core.model.enums.AccountSubType;
import com.biapay.core.model.enums.AccountType;
import java.io.Serializable;
import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class AccountCreateReq implements Serializable {
  private Long accountUserId;
  private String accountNumber;
  private UserType accountUserType;
  private AccountType accountType;
  private AccountSubType accountSubType;
  private AccountStatus accountStatus;
  private String currencyCode;
  private BigDecimal balance;
  private Long settlementMethodId;
  private Long paymentMethodId;
}
