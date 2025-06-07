package com.biapay.accountmanagement.dto;

import com.biapay.core.model.Account;
import com.biapay.core.model.AccountPendingTransaction;
import com.biapay.core.model.AccountTransaction;
import com.biapay.core.model.User;
import com.biapay.core.model.enums.AccountEventType;
import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AccountTransactionDetailsDTO {

  private Account sourceAccount;
  private Account receiverAccount;
  private User sourceUser;
  private User receiverUser;
  private AccountPendingTransaction accountPendingTransaction;
  private AccountTransaction accountTransaction;
}
