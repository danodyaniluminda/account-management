package com.biapay.accountmanagement.service;

import com.biapay.accountmanagement.config.APIError;
import com.biapay.accountmanagement.dto.HoldingTransactionDto;
import com.biapay.accountmanagement.exception.BIABadRequestException;
import com.biapay.accountmanagement.exception.BIARuntimeException;
import com.biapay.accountmanagement.util.DateUtil;
import com.biapay.core.constant.enums.AccountTransactionStatus;
import com.biapay.core.model.Account;
import com.biapay.core.model.AccountEvent;
import com.biapay.core.model.AccountTransaction;
import com.biapay.core.model.Settlement;
import com.biapay.core.model.User;
import com.biapay.core.model.UserType;
import com.biapay.core.model.enums.AccountEventType;
import com.biapay.core.model.enums.AccountSubType;
import com.biapay.core.model.enums.AccountTransactionType;
import com.biapay.core.model.enums.AccountType;
import com.biapay.core.model.enums.SettlementStatus;
import com.biapay.core.repository.AccountEventRepository;
import com.biapay.core.repository.AccountRepository;
import com.biapay.core.repository.AccountTransactionRepository;
import com.biapay.core.repository.SettlementRepository;
import com.biapay.core.repository.UserRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import javax.persistence.OptimisticLockException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@AllArgsConstructor
public class AccountTransactionServiceV2 {

  private final SettlementRepository settlementRepository;
  private final AccountEventRepository accountEventRepository;
  private final AccountRepository accountRepository;
  private final AccountService accountService;
  private final AccountTransactionRepository accountTransactionRepository;
  private final UserRepository userRepository;
  private final SettlementProcessor settlementProcessor;
  private static final int MAX_RETRIES = 3;

  @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class, timeout = 60)
  public void initTransaction(UUID clientTransactionId) {
//    first pull the transaction details from settlement
    try {
      if (clientTransactionId != null) {
        Settlement settlement = settlementRepository.findSettlementForUpdate(clientTransactionId);
        if (SettlementStatus.APPROVED.equals(settlement.getStatus())) {
          // ToDo add check for already data presence in account transaction for fail safe
          try {
            processTransaction(settlement);
          } catch (Exception e) {
            // save failure status in a separate transaction
            accountService.markSettlementAsFailed(clientTransactionId);
            throw e;
          }
        }
      } else {
        log.error("Invalid transaction ref: {} or already processed",
            clientTransactionId.toString());
      }
    } catch (Exception exception) {
      log.error("Failed to process transaction, error:{}", exception.getMessage());
      throw exception;
    }
  }

  public void syncHoldingAmountAfterSettlement(HoldingTransactionDto holdingTransactionDto) {
    holdingTransactionDto.getClientTransactionIds()
        .forEach(
            clientTransactionId -> settlementProcessor.processMerchantHoldingAmountAfterSettlement(
                clientTransactionId));
  }

  private void processTransaction(Settlement settlement) {
    User adminUser = userRepository.findByEmailAndUserType("superadmin@biapay.net", UserType.ADMIN);

    if (settlement.getTotalAmount()
        .subtract(settlement.getSettlementAmount())
        .subtract(settlement.getOperationalFees())
        .subtract(settlement.getFees())
        .subtract(settlement.getTax()).compareTo(BigDecimal.ZERO) != 0) {
      throw new BIABadRequestException("Debit/Credit amount not balanced");
    }

    String currency = settlement.getSettlementCurrency();
    Long posId = settlement.getMerchantPosId();
    Long merchantUserId = settlementProcessor.getMerchantUserId(settlement.getMerchantPosId());
    SettlementStatus settlementStatus;
    AccountTransactionType accountTransactionType = settlementProcessor
        .fetchAccountTransactionType(settlement.getTransactionType());

    Account settlementCollectionAccount = accountService.getSettlementAccount(
        AccountSubType.COLLECTION, currency, null, settlement.getPaymentMethodId());
    Account ledger = accountService.getAccount(adminUser.getUserId(), AccountType.LEDGER, currency);
    // Step 01: (total amount) Settlement debit + Ledger credit
    processTransactionWithRetry(settlement.getClientTransactionId(), currency,
        accountTransactionType,
        DateUtil.from(settlement.getCreatedDate()), settlement.getTotalAmount(),
        "Settlement_Collection to Ledger", settlementCollectionAccount, AccountEventType.DEBIT,
        ledger, AccountEventType.CREDIT, settlement.getMerchantPosId(), false);

    // Step 02: (merchant payable) Ledger debit + Merchant_AVAILABLE / Merchant_HOLD credit
    Account merchant;
    boolean isHolding = false;
    merchant = accountService.getMerchantAccount(merchantUserId, AccountType.MERCHANT, currency,
        posId);
    if (settlement.getSettlementDate().isBefore(LocalDate.now())) {
      settlementStatus = SettlementStatus.COMPLETED;
    } else {
      settlementStatus = SettlementStatus.COMPLETED_ON_HOLD;
      isHolding = true;
    }
    processTransactionWithRetry(
        settlement.getClientTransactionId(), currency, accountTransactionType,
        DateUtil.from(settlement.getCreatedDate()), settlement.getSettlementAmount(),
        "Ledger to merchant payable", ledger, AccountEventType.DEBIT, merchant,
        AccountEventType.CREDIT, settlement.getMerchantPosId(), true);
    settlement.setStatus(settlementStatus);

    // Step 04: (fee) Ledger debit + PSP FEE credit
    Account pspFee = accountService.getPSPFeeAccount(
        AccountSubType.COLLECTION, currency, null, settlement.getPaymentMethodId());
    processTransactionWithRetry(
        settlement.getClientTransactionId(), currency, accountTransactionType,
        DateUtil.from(settlement.getCreatedDate()), settlement.getOperationalFees(),
        "Ledger to psp fee",
        ledger, AccountEventType.DEBIT, pspFee, AccountEventType.CREDIT,
        settlement.getMerchantPosId(), false);

    // Step 04: (fee) Ledger debit + FEE credit
    Account fee = accountService.getAccount(adminUser.getUserId(), AccountType.FEE, currency);
    processTransactionWithRetry(
        settlement.getClientTransactionId(), currency, accountTransactionType,
        DateUtil.from(settlement.getCreatedDate()), settlement.getFees(), "Ledger to fee",
        ledger, AccountEventType.DEBIT, fee, AccountEventType.CREDIT, settlement.getMerchantPosId(),
        false);

    // Step 05: (tax) Ledger debit + Tax credit
    Account tax = accountService.getAccount(adminUser.getUserId(), AccountType.TAX, currency);
    processTransactionWithRetry(
        settlement.getClientTransactionId(), currency, accountTransactionType,
        DateUtil.from(settlement.getCreatedDate()), settlement.getTax(), "Ledger to tax",
        ledger, AccountEventType.DEBIT, tax, AccountEventType.CREDIT, settlement.getMerchantPosId(),
        false);

    // Step 05: update settlement status
    settlementRepository.save(settlement);
  }

  public void processTransactionWithRetry(UUID clientTransactionId,
      String currencyCode,
      AccountTransactionType transactionType,
      LocalDate transactionDate,
      BigDecimal transactionAmount,
      String remarks,
      Account sourceAccount,
      AccountEventType sourceEventType,
      Account receiverAccount,
      AccountEventType receiverEventType,
      Long merchantPosId,
      boolean holdReceiverAmount) {

    int retryCount = 0;
    while (retryCount < MAX_RETRIES) {
      try {
        // Reload accounts for fresh version (important for optimistic locking)
        sourceAccount = accountRepository.findById(sourceAccount.getAccountId())
            .orElseThrow(() -> new BIARuntimeException("Source account not found"));
        receiverAccount = accountRepository.findById(receiverAccount.getAccountId())
            .orElseThrow(() -> new BIARuntimeException("Receiver account not found"));

        BigDecimal sourceBalanceBefore = sourceAccount.getBalance();
        BigDecimal receiverBalanceBefore = receiverAccount.getBalance();

        // Apply balance updates
        if (sourceEventType == AccountEventType.DEBIT) {
          if (!AccountType.SETTLEMENT.equals(sourceAccount.getAccountType()) &&
              sourceBalanceBefore.subtract(transactionAmount).compareTo(BigDecimal.ZERO) < 0) {
            throw new BIABadRequestException(APIError.IN_SUFFICIENT_BALANCE.getDescription());
          }
          sourceAccount.setBalance(sourceBalanceBefore.subtract(transactionAmount));
        } else {
          sourceAccount.setBalance(sourceBalanceBefore.add(transactionAmount));
        }

        if (receiverEventType == AccountEventType.CREDIT) {
          if (AccountType.MERCHANT.equals(receiverAccount.getAccountType()) && holdReceiverAmount) {
            receiverAccount.setOnHold(receiverAccount.getOnHold().add(transactionAmount));
          }
          receiverAccount.setBalance(receiverBalanceBefore.add(transactionAmount));
        } else {
          receiverAccount.setBalance(receiverBalanceBefore.subtract(transactionAmount));
        }

        // Save updated accounts (this triggers optimistic locking)
        accountRepository.saveAndFlush(sourceAccount);
        accountRepository.saveAndFlush(receiverAccount);

        // Create and save AccountTransaction
        AccountTransaction transaction = AccountTransaction.builder()
            .uniqueRef(UUID.randomUUID().toString())
            .transactionRef(clientTransactionId)
            .transactionType(transactionType)
            .transactionStatus(AccountTransactionStatus.COMPLETED)
            .currencyCode(currencyCode)
            .transactionAmount(transactionAmount)
            .transactionDate(transactionDate)
            .processDate(LocalDate.now())
            .isProcessed(true)
            .remarks(remarks)
            .transactionSource(String.valueOf(merchantPosId))
            .createdDate(Instant.now())
            // source
            .sourceAccount(sourceAccount)
            .sourceUserId(sourceAccount.getAccountUserId())
            .sourceUserType(sourceAccount.getAccountUserType())
            .sourceAccountType(sourceAccount.getAccountType())
            .sourceAccountEvent(sourceEventType)
            .sourceAccountBalanceBefore(sourceBalanceBefore)
            .sourceAccountBalance(sourceAccount.getBalance())
            // receiver
            .receiverAccount(receiverAccount)
            .receiverUserId(receiverAccount.getAccountUserId())
            .receiverUserType(receiverAccount.getAccountUserType())
            .receiverAccountType(receiverAccount.getAccountType())
            .receiverAccountEvent(receiverEventType)
            .receiverAccountBalanceBefore(receiverBalanceBefore)
            .receiverAccountBalance(receiverAccount.getBalance())
            .build();
        accountTransactionRepository.save(transaction);

        // Log AccountEvents
        accountEventRepository.save(AccountEvent.builder()
            .accountId(sourceAccount.getAccountId())
            .accountType(sourceAccount.getAccountType())
            .eventType(sourceEventType)
            .accountTransactionId(transaction.getId())
            .amount(transactionAmount)
            .balanceBeforeTransaction(sourceBalanceBefore)
            .balanceAfterTransaction(sourceAccount.getBalance())
            .build());

        accountEventRepository.save(AccountEvent.builder()
            .accountId(receiverAccount.getAccountId())
            .accountType(receiverAccount.getAccountType())
            .eventType(receiverEventType)
            .accountTransactionId(transaction.getId())
            .amount(transactionAmount)
            .balanceBeforeTransaction(receiverBalanceBefore)
            .balanceAfterTransaction(receiverAccount.getBalance())
            .build());

        log.info("Transaction completed: {} {} from {}:{} to {}:{}",
            transactionAmount, currencyCode,
            sourceAccount.getAccountType(), sourceAccount.getAccountUserId(),
            receiverAccount.getAccountType(), receiverAccount.getAccountUserId());
        return;

      } catch (OptimisticLockException e) {
        retryCount++;
        log.warn("Optimistic lock error (attempt {}/{}), retrying...", retryCount, MAX_RETRIES);
      } catch (Exception ex) {
        log.error("Transaction failed: {}", ex.getMessage(), ex);
        throw new BIARuntimeException("Transaction processing failed");
      }
    }

    throw new BIARuntimeException(
        "Transaction failed after maximum retries due to concurrent updates");
  }


}
