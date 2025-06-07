package com.biapay.accountmanagement.service;

import com.biapay.accountmanagement.config.APIError;
import com.biapay.accountmanagement.dto.AccountTransactionDetailsDTO;
import com.biapay.accountmanagement.dto.CreditDebitTransactionRes;
import com.biapay.accountmanagement.exception.BIABadRequestException;
import com.biapay.core.constant.enums.AccountTransactionStatus;
import com.biapay.core.model.Account;
import com.biapay.core.model.AccountEvent;
import com.biapay.core.model.AccountPendingTransaction;
import com.biapay.core.model.AccountTransaction;
import com.biapay.core.model.AccountTransaction.AccountTransactionBuilder;
import com.biapay.core.model.User;
import com.biapay.core.model.UserType;
import com.biapay.core.model.enums.AccountEventType;
import com.biapay.core.model.enums.AccountType;
import com.biapay.core.repository.AccountEventRepository;
import com.biapay.core.repository.AccountPendingTransactionRepository;
import com.biapay.core.repository.AccountRepository;
import com.biapay.core.repository.AccountTransactionRepository;
import com.biapay.core.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@AllArgsConstructor
public class AccountTransactionService {

  private final UserRepository userRepository;
  private final AccountRepository accountRepository;
  private final AccountService accountService;
  private final AccountTransactionRepository accountTransactionRepository;
  private final AccountPendingTransactionRepository accountPendingTransactionRepository;
  private final AccountEventRepository accountEventRepository;


  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void initTransaction(String transactionRef) {
//    first pull the pending transaction and save the transaction log to account_transactions table

    try {
      if (transactionRef != null) {
        List<AccountPendingTransaction> accountPendingTransactions = getPendingTransactions(
            UUID.fromString(transactionRef));

        for (AccountPendingTransaction accountPendingTransaction : accountPendingTransactions) {
          processTransaction(accountPendingTransaction);
        }

      } else {
        log.error("Invalid transaction ref: {} or already processed", transactionRef);
      }
    } catch (Exception exception) {
      log.error("Failed to process transaction, error:{}", exception.getMessage());
    }

  }

  private void processTransaction(AccountPendingTransaction accountPendingTransaction) {
    log.info("Initiate transaction for ref: {}", accountPendingTransaction.getReference());
    if (accountPendingTransaction.getAmount().compareTo(BigDecimal.ZERO) > 0) {
      AccountTransactionDetailsDTO accountTransactionDetails = mapAccountTransaction(
          accountPendingTransaction);

      Account sourceAccount = accountTransactionDetails.getSourceAccount();
      Account receiverAccount = accountTransactionDetails.getReceiverAccount();
      AccountTransaction accountTransaction = accountTransactionDetails.getAccountTransaction();

      CreditDebitTransactionRes creditTransactionRes = CreditDebitTransactionRes.builder()
          .success(false).build();

      CreditDebitTransactionRes debitTransactionRes = CreditDebitTransactionRes.builder()
          .success(false).build();

      log.info("Saving transaction history");
      //      save to transaction history
      AccountTransaction transactionHistory = saveTransactionHistory(
          accountTransactionDetails.getSourceUser(),
          accountTransactionDetails.getReceiverUser(),
          accountTransactionDetails.getSourceAccount(),
          accountTransactionDetails.getReceiverAccount(),
          accountTransaction
      );

      try {
        log.info("Initiating credit and debit operation on sourceAccount");
        if (sourceAccount != null) {
          if (accountTransaction.getSourceAccountEvent() == AccountEventType.CREDIT) {
            creditTransactionRes = doTransaction(AccountEventType.CREDIT,
                sourceAccount,
                accountTransaction.getAmount(),
                transactionHistory.getId());
          }
          if (accountTransaction.getSourceAccountEvent() == AccountEventType.DEBIT) {
            debitTransactionRes = doTransaction(AccountEventType.DEBIT,
                sourceAccount,
                accountTransaction.getAmount(),
                transactionHistory.getId());
          }

        } else {
          log.error("Not found any sourceAccount!");
        }

        log.info("Initiating credit and debit operation on receiverAccount");
        if (receiverAccount != null) {

          if (accountTransaction.getReceiverAccountEvent() == AccountEventType.CREDIT) {
            creditTransactionRes = doTransaction(AccountEventType.CREDIT,
                receiverAccount,
                accountTransaction.getAmount(),
                transactionHistory.getId());
          }
          if (accountTransaction.getReceiverAccountEvent() == AccountEventType.DEBIT) {
            debitTransactionRes = doTransaction(AccountEventType.DEBIT,
                receiverAccount,
                accountTransaction.getAmount(),
                transactionHistory.getId());
          }

        } else {
          log.error("Not found any receiverAccount!");
        }

        transactionHistory.setTransactionStatus(AccountTransactionStatus.HOLD);
        accountTransactionRepository.save(transactionHistory);

        log.info("Finished initial transaction. Status = success");

        if (creditTransactionRes.isSuccess() || debitTransactionRes.isSuccess()) {
          log.info("Initiate fees and tax accounting");
          Account ledgerAccount = getLedgerAccount(accountTransaction.getCurrencyCode());

          if (ledgerAccount != null) {
            //        2.	Debit merchant account and Credit Ledger Account with the fees paid by the user (if any)
            if (accountTransaction.getFees() != null
                && accountTransaction.getFees().compareTo(BigDecimal.ZERO) > 0) {
              doTransaction(AccountEventType.DEBIT, sourceAccount,
                  accountTransaction.getFees(),
                  transactionHistory.getId());
              doTransaction(AccountEventType.CREDIT, ledgerAccount,
                  accountTransaction.getFees(),
                  transactionHistory.getId());
            } else {
              log.info("Fee=0 for transactionId: {}", transactionHistory.getId());
            }

            if (accountTransaction.getTransactionFees() != null
                && accountTransaction.getTransactionFees().compareTo(BigDecimal.ZERO) > 0) {
              doTransaction(AccountEventType.DEBIT, sourceAccount,
                  accountTransaction.getTransactionFees(),
                  transactionHistory.getId());
              doTransaction(AccountEventType.CREDIT, ledgerAccount,
                  accountTransaction.getTransactionFees(),
                  transactionHistory.getId());
            } else {
              log.info("Transaction fees=0 for transactionId: {}",
                  transactionHistory.getId());
            }

            if (accountTransaction.getOperationFees() != null
                && accountTransaction.getOperationFees().compareTo(BigDecimal.ZERO) > 0) {
              doTransaction(AccountEventType.DEBIT, sourceAccount,
                  accountTransaction.getOperationFees(),
                  transactionHistory.getId());
              doTransaction(AccountEventType.CREDIT, ledgerAccount,
                  accountTransaction.getOperationFees(),
                  transactionHistory.getId());
            } else {
              log.info("Operation fees=0 for transactionId: {}",
                  transactionHistory.getId());
            }

            if (accountTransaction.getTax() != null
                && accountTransaction.getTax().compareTo(BigDecimal.ZERO) > 0) {
              doTransaction(AccountEventType.DEBIT, sourceAccount,
                  accountTransaction.getTax(),
                  transactionHistory.getId());
              doTransaction(AccountEventType.CREDIT, ledgerAccount,
                  accountTransaction.getTax(),
                  transactionHistory.getId());
            } else {
              log.info("Tax=0 for transactionId: {}", transactionHistory.getId());
            }
          } else {
            log.error("No ledger account found for {}",
                accountTransaction.getCurrencyCode());
          }

          log.info("Finished tax and fees deduction");
        } else {
          log.info("Either initial credit or debit transaction is failed");
        }

        transactionHistory.setTransactionStatus(AccountTransactionStatus.COMPLETED);
        transactionHistory.setIsProcessed(true);
        transactionHistory.setProcessDate(LocalDate.now());
        accountTransactionRepository.save(transactionHistory);

        accountPendingTransaction.setTransactionStatus(AccountTransactionStatus.COMPLETED);
        accountPendingTransaction.setProcessDate(LocalDate.now());
        accountPendingTransaction.setIsProcessed(true);
        accountPendingTransactionRepository.save(accountPendingTransaction);

      } catch (Exception ex) {
        log.error("Failed to initiate transaction, error: {}", ex.getMessage());
//        TODO reverse

      }

    } else {
      log.error("Amount=0, transaction is skipped");
    }
  }

  public List<AccountPendingTransaction> getPendingTransactions(UUID transactionId) {
    return accountPendingTransactionRepository.findAllByTransactionIdAndTransactionStatus(
        transactionId, AccountTransactionStatus.PENDING);
  }

  private AccountTransactionDetailsDTO mapAccountTransaction(
      AccountPendingTransaction accountPendingTransaction) {

    log.info("Mapping Account Transaction for: {} ", accountPendingTransaction.getTransactionId());
//    first check if same transaction ref is exists and completed, then skip this
    if (!accountTransactionRepository.existsAccountTransactionByUniqueRefAndTransactionStatus(
        accountPendingTransaction.getUniqueRef(), AccountTransactionStatus.COMPLETED)) {
      log.info("Valid transaction ID: {}, sourceUserId: {}, receiverUserId: {} ",
          accountPendingTransaction.getTransactionId(), accountPendingTransaction.getSourceUserId(),
          accountPendingTransaction.getReceiverUserId());
//    Account sourceAccount = getAccount(
//        accountPendingTransaction.getSourceUserId(),
//        accountPendingTransaction.getSourceAccountType(),
//        accountPendingTransaction.getCurrencyCode()
//    );
//    Account receiverAccount = getAccount(accountPendingTransaction.getReceiverUserId(),
//        accountPendingTransaction.getReceiverAccountType(),
//        accountPendingTransaction.getCurrencyCode());

      //Validate from and to account
      User sourceUser = getUser(accountPendingTransaction.getSourceUserId(),
          accountPendingTransaction.getSourceAccountType());
      User receiverUser = getUser(accountPendingTransaction.getReceiverUserId(),
          accountPendingTransaction.getReceiverAccountType());
      Account sourceAccount = getAccount(accountPendingTransaction.getSourceUserId(),
          accountPendingTransaction.getSourceAccountType(),
          accountPendingTransaction.getCurrencyCode());
      Account receiverAccount = getAccount(accountPendingTransaction.getReceiverUserId(),
          accountPendingTransaction.getReceiverAccountType(),
          accountPendingTransaction.getCurrencyCode());

      AccountTransactionBuilder accountTransactionBuilder = AccountTransaction.builder();

      accountTransactionBuilder
//          .uniqueRef(StringUtil.getTransactionReference())
          .uniqueRef(accountPendingTransaction.getUniqueRef())
          .transactionRef(accountPendingTransaction.getTransactionId())
          .initUserId(sourceUser == null ? (receiverUser == null ? 0 : receiverUser.getUserId())
              : sourceUser.getUserId())
          .initUserType(sourceUser == null ? (receiverUser == null ? UserType.ADMIN
              : receiverUser.getUserType()) : sourceUser.getUserType());

      if (sourceAccount != null && sourceUser != null) {
        accountTransactionBuilder
            .sourceAccount(sourceAccount)
            .sourceUserId(accountPendingTransaction.getSourceUserId())
            .sourceUsername(sourceUser.getName())
            .sourceMobileNumber(sourceUser.getMobileNumber())
            .sourceUserType(sourceUser.getUserType())
            .sourceAccountType(sourceAccount.getAccountType())
            .sourceAccountEvent(accountPendingTransaction.getSourceAccountEvent())
            .sourceAccountBalanceBefore(sourceAccount.getBalance());
      }

      if (receiverAccount != null && receiverUser != null) {
        accountTransactionBuilder
            .sourceAccount(sourceAccount)
            .receiverAccount(receiverAccount)
            .receiverUserId(accountPendingTransaction.getReceiverUserId())
            .receiverUsername(receiverUser.getName())
            .receiverMobileNumber(receiverUser.getMobileNumber())
            .receiverUserType(receiverUser.getUserType())
            .receiverAccountType(receiverAccount.getAccountType())
            .receiverAccountEvent(accountPendingTransaction.getReceiverAccountEvent())
            .receiverAccountBalanceBefore(receiverAccount.getBalance());
      }

      accountTransactionBuilder
          .currencyCode(accountPendingTransaction.getCurrencyCode())
          .amount(accountPendingTransaction.getAmount())
          .transactionAmount(accountPendingTransaction.getTransactionFees())
          .fees(accountPendingTransaction.getFees())
          .operationFees(accountPendingTransaction.getOperationFees())
          .transactionFees(accountPendingTransaction.getTransactionFees())
          .otherFees(accountPendingTransaction.getOtherFees())
          .tax(accountPendingTransaction.getTax())

          .transactionSource(accountPendingTransaction.getTransactionSource())
          .transactionType(accountPendingTransaction.getTransactionType())
          .transactionStatus(accountPendingTransaction.getTransactionStatus())

          .narration(accountPendingTransaction.getNarration())
          .remarks(accountPendingTransaction.getRemarks())

          .transactionDate(accountPendingTransaction.getTransactionDate())
          .processDate(LocalDate.now())
          .isProcessed(false)
          .createdBy(accountPendingTransaction.getCreatedBy())
          .createdDate(accountPendingTransaction.getCreatedDate())
          .lastModifiedBy(accountPendingTransaction.getLastModifiedBy())
          .lastModifiedDate(accountPendingTransaction.getLastModifiedDate())

          .build();

      return AccountTransactionDetailsDTO.builder()
          .accountPendingTransaction(accountPendingTransaction)
          .accountTransaction(accountTransactionRepository.save(accountTransactionBuilder.build()))
          .sourceAccount(sourceAccount)
          .receiverAccount(receiverAccount)
          .sourceUser(sourceUser)
          .receiverUser(receiverUser)
          .build();
    } else {
      log.error("Transaction is already process for ref: {}",
          accountPendingTransaction.getTransactionId());
      return null;
    }

  }

  private User getUser(Long userId, AccountType accountType) {
    if (userId != null && userId != 0) {

      log.info("Source account userId is not null and not 0");

      return userRepository.findByUserId(userId).orElse(null);
    } else if (accountType == AccountType.LEDGER) {
      return userRepository.findByEmailAndUserType("biapay@groupebia.com", UserType.MERCHANT);
    }
    log.error("No user found with id {}", userId);
    return null;
  }

  private Account getAccount(Long userId, AccountType accountType, String currencyCode) {

    if (userId == null && accountType == null) {
      return null;
    }

    if (userId != null) {
      if (accountType == AccountType.LEDGER) {
        return accountService.getLedgerAccount(
            currencyCode
        ).orElse(null);
      } else {
        return accountService.getAccount(
            userId,
            accountType,
            currencyCode
        );
      }
    } else {
      if (accountType == AccountType.LEDGER) {
        return accountService.getLedgerAccount(
            currencyCode
        ).orElse(null);
      }

      log.error("No account found for userId {}, accountType, currency:{}", userId);
    }

    return null;
  }

  private Account getLedgerAccount(String currencyCode) {
    return accountService.getLedgerAccount(
        currencyCode
    ).orElse(null);
  }

//
//
//  private void creditOperation(AccountTransactionReqest accountTransactionReq,
//      AccountTransaction accountTransaction, Account ledgerAccount, Account fromAccount,
//      Account toAccount) {
//    //        1.	Credit Ledger Account with the base amount
//    creditAccount(accountTransactionReq, fromUser, toUser, fromAccount, ledgerAccount,
//        accountTransactionReq.getAmount());
//

//
////        3.	Credit Merchant Account with the base amount
//    creditAccount(accountTransaction, toAccount,
//        accountTransactionReq.getAmount());
//
////        4.	Debit User Account if User is a BIAPAY Customer
//    if (fromAccount != null) {
//      debitAccount(accountTransaction, fromAccount,
//          accountTransactionReq.getAmount());
//    }
//  }
//
//  private void debitOperation(AccountTransactionReqest accountTransactionReq,
//      AccountTransaction accountTransaction, Account ledgerAccount, Account fromAccount,
//      Account toAccount) {
//    //        1.	Debit Ledger Account with the settled amount
//    debitAccount(accountTransaction, ledgerAccount,
//        accountTransactionReq.getAmount());
//
////        2.	Debit Merchant Account with the settled amount
//    debitAccount(accountTransaction, fromAccount,
//        accountTransactionReq.getAmount());
//
////        3.	Debit Merchant Account with transactions fees pay by the merchant (if not paid by user)
//    if (accountTransactionReq.getOperationFees().compareTo(BigDecimal.ZERO) > 0) {
//      debitAccount(accountTransaction, fromAccount,
//          accountTransactionReq.getOperationFees());
//    }
//
//    if (accountTransactionReq.getTransactionFees().compareTo(BigDecimal.ZERO) > 0) {
//      debitAccount(accountTransaction, fromAccount,
//          accountTransactionReq.getTransactionFees());
//    }
//
////        TODO:: 4.	Debit Merchant Account with plan fees (if any pending plan fees due to auto-renew of plan)
//
////        5.	Debit Merchant Account with tax (if any)
//    debitAccount(accountTransaction, fromAccount,
//        accountTransactionReq.getTax());
//
////        6.	Credit User Account with paid amount including fees (if any) if User is a BIAPAY Customer
//    if (toAccount != null) {
//      debitAccount(accountTransaction, toAccount,
//          accountTransactionReq.getAmount());
//    }
//  }


  private CreditDebitTransactionRes doTransaction(AccountEventType accountEvent, Account account,
      BigDecimal amount,
      Long transactionId) {
    CreditDebitTransactionRes transactionRes = CreditDebitTransactionRes.builder()
        .success(false).build();
    transactionRes.setAccount(account);
    try {
      if (accountEvent == AccountEventType.CREDIT) {
        transactionRes.setEventType(AccountEventType.CREDIT);
        transactionRes.setBalanceBefore(account.getBalance());
        account.setBalance(account.getBalance().add(amount));

        transactionRes.setBalanceAfter(account.getBalance().add(amount));
        log.info("account credited for: {}:{}", account.getAccountUserType(),
            account.getAccountUserId());
//        AccountCreditedEvent accountCreditedEvent = new AccountCreditedEvent();
//        accountCreditedEvent.setAccountId();
//        accountCreditedEvent.setAccountTransactionId();
//        accountCreditedEvent.setAccountType();
//        accountCreditedEvent.setAmount();
//        accountCreditedEvent.setBalanceBefore();
//        accountCreditedEvent.setBalanceAfter();
//        EventUtil.publishEvent(accountCreditedEvent);

        AccountEvent accountCreditedEvent =
            AccountEvent.builder()
                .accountId(account.getAccountId())
                .accountType(account.getAccountType())
                .eventType(AccountEventType.CREDIT)
                .accountTransactionId(transactionId)
                .amount(amount)
                .balanceBeforeTransaction(transactionRes.getBalanceBefore())
                .balanceAfterTransaction(transactionRes.getBalanceAfter())
                .build();
        accountEventRepository.save(accountCreditedEvent);

      }
      if (accountEvent == AccountEventType.DEBIT) {
        transactionRes.setEventType(AccountEventType.DEBIT);
        if (account.getBalance().subtract(amount).compareTo(BigDecimal.ZERO) <= 0) {
          throw new BIABadRequestException(APIError.IN_SUFFICIENT_BALANCE.getDescription());
        }

        transactionRes.setBalanceBefore(account.getBalance());

        account.setBalance(account.getBalance().subtract(amount));

        transactionRes.setBalanceAfter(account.getBalance().subtract(amount));

//        AccountDebitedEvent accountDebitedEvent = new AccountDebitedEvent();
//        accountDebitedEvent.setAccountId(account.getAccountId());
//        accountDebitedEvent.setAccountTransactionId(transactionId);
//        accountDebitedEvent.setAccountType(account.getAccountType());
//        accountDebitedEvent.setAmount(amount);
//        accountDebitedEvent.setBalanceBefore(transactionRes.getBalanceBefore());
//        accountDebitedEvent.setBalanceAfter(transactionRes.getBalanceAfter());
//
//        EventUtil.publishEvent(accountDebitedEvent);
        AccountEvent accountDebitedEvent =
            AccountEvent.builder()
                .accountId(account.getAccountId())
                .accountType(account.getAccountType())
                .eventType(AccountEventType.DEBIT)
                .accountTransactionId(transactionId)
                .amount(amount)
                .balanceBeforeTransaction(transactionRes.getBalanceBefore())
                .balanceAfterTransaction(transactionRes.getBalanceAfter())
                .build();
        accountEventRepository.save(accountDebitedEvent);
      }

      accountRepository.save(account);
      log.info("Account has been top up: {}", account.getAccountUserId());

      transactionRes.setSuccess(true);
    } catch (Exception ex) {
      transactionRes.setSuccess(false);
      log.error("Failed to do {} transaction, error:{}", accountEvent.toString(), ex.getMessage());
    }

    return transactionRes;
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public AccountTransaction saveTransactionHistory(
      User fromUser,
      User toUser,
      Account fromAccount,
      Account toAccount,
      AccountTransaction accountTransaction) {
    accountTransaction.setSourceAccount(fromAccount);
    accountTransaction.setCurrencyCode(accountTransaction.getCurrencyCode());
    accountTransaction.setFees(accountTransaction.getFees());
//    accountTransaction.setCommission(commission);
    accountTransaction.setReceiverAccount(toAccount);
    accountTransaction.setTransactionType(accountTransaction.getTransactionType());

    if (fromUser != null || fromAccount != null) {
      accountTransaction.setSourceUserType(fromUser.getUserType());
      accountTransaction.setSourceUserId(fromUser.getUserId());

      accountTransaction.setSourceAccountBalanceBefore(fromAccount.getBalance());
      accountTransaction.setSourceUsername(fromAccount.getAccountNumber());
      accountTransaction.setSourceMobileNumber(fromUser.getMobileNumber());
      accountTransaction.setSourceAccountType(fromAccount.getAccountType());
      accountTransaction.setSourceAccountEvent(accountTransaction.getSourceAccountEvent());
    }

    if (toUser != null || toAccount != null) {
      accountTransaction.setReceiverUserType(toUser.getUserType());
      accountTransaction.setReceiverUserId(toUser.getUserId());

      accountTransaction.setReceiverAccountBalanceBefore(toAccount.getBalance());
      accountTransaction.setReceiverUsername(toAccount.getAccountNumber());
      accountTransaction.setReceiverMobileNumber(toUser.getMobileNumber());
      accountTransaction.setReceiverAccountType(toAccount.getAccountType());
      accountTransaction.setReceiverAccountEvent(
          accountTransaction.getReceiverAccountEvent());
//      accountTransaction.setToAccountBalanceBefore(toAccount.getBalance());
    }

    accountTransaction.setTransactionType(accountTransaction.getTransactionType());
    accountTransaction.setAmount(accountTransaction.getAmount());
    accountTransaction.setTransactionStatus(AccountTransactionStatus.INITIATED);
    accountTransaction.setRemarks(accountTransaction.getRemarks());

    return accountTransactionRepository.save(accountTransaction);

  }

//  @Transactional(propagation = Propagation.REQUIRED)
//  public AccountTransaction completeTxn(AccountTransaction accountTransaction) {
//    accountTransactionRepository.save(accountTransaction);
//    return accountTransaction;
//  }
}
