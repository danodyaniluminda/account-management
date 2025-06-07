package com.biapay.accountmanagement.dto;

import com.biapay.core.model.Account;
import com.biapay.core.model.enums.AccountEventType;
import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreditDebitTransactionRes {

  private Account account;
  private BigDecimal balanceBefore;
  private BigDecimal balanceAfter;
  private AccountEventType eventType;
  private boolean success;
}
