package com.biapay.accountmanagement.service;

import com.biapay.accountmanagement.config.APIError;
import com.biapay.accountmanagement.exception.BIABadRequestException;
import com.biapay.accountmanagement.exception.BIARuntimeException;
import com.biapay.core.constant.enums.MerchantStatus;
import com.biapay.core.model.Account;
import com.biapay.core.model.MerchantPOS;
import com.biapay.core.model.MerchantSubscriptionCompleted;
import com.biapay.core.model.Settlement;
import com.biapay.core.model.Shop;
import com.biapay.core.model.enums.AccountTransactionType;
import com.biapay.core.model.enums.AccountType;
import com.biapay.core.model.enums.SettlementStatus;
import com.biapay.core.repository.AccountRepository;
import com.biapay.core.repository.MerchantPOSRepository;
import com.biapay.core.repository.SettlementRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.persistence.OptimisticLockException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@AllArgsConstructor
public class SettlementProcessor {
  private final SettlementRepository settlementRepository;
  private final AccountService accountService;
  private final AccountRepository accountRepository;
  private final MerchantPOSRepository merchantPOSRepository;
  private static final int MAX_RETRIES = 3;

  private static final Map<String, AccountTransactionType> TRANSACTION_TYPE_MAP = Map.ofEntries(
      Map.entry("MERCHANT_PAYMENT", AccountTransactionType.CASH_IN),
      Map.entry("IN_STORE_PAYMENT", AccountTransactionType.CASH_IN),
      Map.entry("EVENT_PAYMENT", AccountTransactionType.CASH_IN),
      Map.entry("REFUND", AccountTransactionType.CASH_OUT),
      Map.entry("BILL_PAYMENT", AccountTransactionType.BILL_PAYMENT),
      Map.entry("PAYLINK_REQUEST", AccountTransactionType.REQUEST_MONEY_PAY),
      Map.entry("INVOICE_PAYMENT", AccountTransactionType.REQUEST_MONEY_PAY),
      Map.entry("DISBURSEMENT", AccountTransactionType.DISBURSEMENT)
  );

  @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class, timeout = 10)
  public void processMerchantHoldingAmountAfterSettlement(String clientTransactionId) {
    int retryCount = 0;

    while (retryCount < MAX_RETRIES) {
      try {
        Settlement settlement = settlementRepository
            .findSettlementForUpdate(UUID.fromString(clientTransactionId));

        if (!SettlementStatus.COMPLETED_ON_HOLD.equals(settlement.getStatus())) {
          return; // Nothing to do
        }

        String currency = settlement.getSettlementCurrency();
        Long posId = settlement.getMerchantPosId();
        Long merchantUserId = getMerchantUserId(posId);
        BigDecimal transactionAmount = settlement.getSettlementAmount();

        // Reload fresh version inside the loop for optimistic locking
        Account merchantAccount = accountRepository.findByAccountUserIdAndAccountTypeAndCurrencyCodeAndPosId(merchantUserId, AccountType.MERCHANT, currency, posId)
            .orElseThrow(() -> new BIARuntimeException("Merchant account not found"));

        BigDecimal updatedOnHold = merchantAccount.getOnHold().subtract(transactionAmount);
        if (updatedOnHold.compareTo(BigDecimal.ZERO) < 0) {
          throw new BIABadRequestException(APIError.IN_SUFFICIENT_BALANCE.getDescription());
        }

        merchantAccount.setOnHold(updatedOnHold);
        accountRepository.saveAndFlush(merchantAccount); // triggers version check

        // After successful balance update, update settlement (no version check here)
        settlement.setStatus(SettlementStatus.COMPLETED);
        settlementRepository.save(settlement);

        log.info("Successfully released holding amount for transaction {}", clientTransactionId);
        return;

      } catch (OptimisticLockException e) {
        retryCount++;
        log.warn("OptimisticLockException on merchant account. Retry {}/{}", retryCount, MAX_RETRIES);
      } catch (Exception ex) {
        log.error("Error while processing holding for {}: {}", clientTransactionId, ex.getMessage(), ex);
        throw ex;
      }
    }

    throw new BIARuntimeException("Max retries exceeded while processing holding amount");
  }

  private boolean validatePosAndMerchantStatus(MerchantPOS merchantPOS) {
    List<MerchantStatus> merchantEnabledStatusList = List.of(MerchantStatus.APPROVED, MerchantStatus.ACTIVE);
    try {
      Shop shop = merchantPOS.getShop();

      //check for merchant active plan
      MerchantSubscriptionCompleted msc = merchantPOS.getMerchantSubscriptionCompleted();
      if (msc != null && (!msc.isActive() || msc.getEndDate().isBefore(LocalDateTime.now()))) {
        return false;
      }

      return shop.getStatus() && merchantPOS.getStatus() && merchantPOS.getEnable() &&
          merchantEnabledStatusList.contains(shop.getMerchant().getMerchantStatus());

    } catch (Exception ex) {
      log.error("Failed to validatePosAndMerchantStatus, error", ex);
      return false;
    }
  }

  public Long getMerchantUserId(Long merchantPosId) {
    MerchantPOS merchantPOS = merchantPOSRepository.findMerchantPOSWithSubscription(merchantPosId);
    if (!validatePosAndMerchantStatus(merchantPOS)) {
      throw new BIABadRequestException("merchant or Shop or POS or plan is not active for merchant "
          + "pos " + merchantPOS.getName());
    }
    return merchantPOS.getShop().getMerchant().getRootUser().getUserId();
  }

  public AccountTransactionType fetchAccountTransactionType(String transactionType) {
    return TRANSACTION_TYPE_MAP.get(transactionType);
  }
}
