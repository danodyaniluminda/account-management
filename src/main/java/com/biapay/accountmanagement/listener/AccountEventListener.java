package com.biapay.accountmanagement.listener;

import com.biapay.accountmanagement.event.AccountCreditedEvent;
import com.biapay.accountmanagement.event.AccountDebitedEvent;
import com.biapay.core.model.AccountEvent;
import com.biapay.core.repository.AccountEventRepository;
import com.biapay.core.model.enums.AccountEventType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class AccountEventListener {

  private final AccountEventRepository accountEventRepository;

  public AccountEventListener(
      AccountEventRepository accountEventRepository) {
    this.accountEventRepository = accountEventRepository;
  }

  @EventListener(classes = AccountCreditedEvent.class)
  @Transactional(propagation = Propagation.REQUIRED)
  @Async
  public void processAccountCreditedEvent(AccountCreditedEvent accountCreditedEvent) {
    log.info("Received accountCreditedEvent: {}", accountCreditedEvent);

    AccountEvent accountEvent =
        AccountEvent.builder()
            .accountId(accountCreditedEvent.getAccountId())
            .accountType(accountCreditedEvent.getAccountType())
            .eventType(AccountEventType.CREDIT)
            .accountTransactionId(accountCreditedEvent.getAccountTransactionId())
            .amount(accountCreditedEvent.getAmount())
            .balanceBeforeTransaction(accountCreditedEvent.getBalanceBefore())
            .balanceAfterTransaction(accountCreditedEvent.getBalanceAfter())
            .build();
    accountEventRepository.save(accountEvent);
  }

  @EventListener(classes = AccountDebitedEvent.class)
  @Transactional(propagation = Propagation.REQUIRED)
  @Async
  public void processAccountDebitedEvent(AccountDebitedEvent accountDebitedEvent) {
    log.info("Received accountDebitedEvent: {}", accountDebitedEvent);

    AccountEvent accountEvent =
        AccountEvent.builder()
            .accountId(accountDebitedEvent.getAccountId())
            .accountType(accountDebitedEvent.getAccountType())
            .eventType(AccountEventType.DEBIT)
            .accountTransactionId(accountDebitedEvent.getAccountTransactionId())
            .amount(accountDebitedEvent.getAmount())
            .balanceBeforeTransaction(accountDebitedEvent.getBalanceBefore())
            .balanceAfterTransaction(accountDebitedEvent.getBalanceAfter())
            .build();
    accountEventRepository.save(accountEvent);
  }
}
