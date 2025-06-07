package com.biapay.accountmanagement.event;

import com.biapay.core.model.Account;
import com.biapay.core.dto.AccountTransactionRequest;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AccountToAccountTransferCompletedEvent {

  private AccountTransactionRequest accountToAccountReq;
  private Account debitAccount;
}
