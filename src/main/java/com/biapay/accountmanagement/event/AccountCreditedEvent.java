package com.biapay.accountmanagement.event;

import com.biapay.core.model.enums.AccountType;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccountCreditedEvent {

  private Long accountId;
  private Long accountTransactionId;
  private AccountType accountType;
  private BigDecimal amount;
  private BigDecimal balanceBefore;
  private BigDecimal balanceAfter;
}
