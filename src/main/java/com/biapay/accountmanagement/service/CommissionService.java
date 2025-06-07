//package com.biapay.accountmanagement.service;
//
//import static com.biapay.accountmanagement.config.APIError.ACCOUNT_ALREADY_EXISTS;
//
//import com.biapay.accountmanagement.config.Constants;
//import com.biapay.accountmanagement.dto.InternalAccountBalanceReq;
//import com.biapay.accountmanagement.dto.LoggedInUser;
//import com.biapay.accountmanagement.dto.AccountCreateReq;
//import com.biapay.accountmanagement.dto.AccountEventDTO;
//import com.biapay.accountmanagement.dto.AccountHistoryReq;
//import com.biapay.accountmanagement.dto.AccountRes;
//import com.biapay.accountmanagement.dto.AccountUpdateReq;
//import com.biapay.accountmanagement.enums.AccountType;
//import com.biapay.accountmanagement.exception.BIABadRequestException;
//import com.biapay.accountmanagement.mapper.AccountEventMapper;
//import com.biapay.accountmanagement.mapper.AccountMapper;
//import com.biapay.core.model.Account;
//import com.biapay.core.model.AccountEvent;
//import com.biapay.core.model.AccountTransaction;
//import com.biapay.core.repository.AccountEventRepository;
//import com.biapay.accountmanagement.repository.AccountRepository;
//import com.biapay.core.model.UserType;
//import java.math.BigDecimal;
//import java.time.Instant;
//import java.time.ZoneOffset;
//import java.util.List;
//import java.util.stream.Collectors;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.PlatformTransactionManager;
//import org.springframework.transaction.TransactionStatus;
//import org.springframework.transaction.support.DefaultTransactionDefinition;
//
//@Service
//@Slf4j
//public class CommissionService {
//
//  @Autowired
//  private PlatformTransactionManager platformTransactionManager;
//  @Autowired
//  private AccountRepository accountRepository;
//  @Autowired
//  private AccountEventRepository accountEventRepository;
//
//  public void createCommissionAccount(AccountCreateReq accountCreateReq) {
//    Account account =
//        Account.builder()
//            .balance(BigDecimal.ZERO)
//            .countryCode(accountCreateReq.getCountryCode())
//            .currencyCode(accountCreateReq.getCurrencyCode())
//            .accountUserType(accountCreateReq.getAccountUserType())
//            .accountUserId(accountCreateReq.getAccountUserId())
//            .accountType(AccountType.COMMISSION)
//            .build();
//    accountRepository.save(account);
//  }
//
//  public void updateCommissionAccount(AccountUpdateReq accountCreateReq) {
//    TransactionStatus transactionStatus =
//        platformTransactionManager.getTransaction(new DefaultTransactionDefinition());
//    try {
//      accountRepository.updateAccount(
//          accountCreateReq.getNewPhoneNumber(),
//          accountCreateReq.getPhoneNumber(),
//          accountCreateReq.getAccountUserType());
//      platformTransactionManager.commit(transactionStatus);
//    } catch (Exception e) {
//      log.error(e.getMessage(), e);
//      platformTransactionManager.rollback(transactionStatus);
//    }
//  }
//
//  public Account loggedInUserCommissionAccount(LoggedInUser loggedInUser) {
//    return accountRepository
//        .findFirstByCountryCodeAndCurrencyCodeAndAccountUserTypeAndAccountUserId(
//            Constants.DEFAULT_COUNTRY_CODE,
//            Constants.DEFAULT_CURRENCY_CODE,
//            loggedInUser.getUserType(),
//            loggedInUser.getUserId())
//        .orElseThrow(() -> new BIABadRequestException(ACCOUNT_ALREADY_EXISTS.getDescription()));
//  }
//
//  public List<AccountRes> getAccounts(LoggedInUser loggedInUser) {
//    List<Account> accounts =
//        accountRepository.findAllByAccountUserIdAndAccountUserType(
//            loggedInUser.getUserId(), loggedInUser.getUserType());
//    return accounts.stream()
//        .map(AccountMapper.INSTANCE::toAccountRes)
//        .collect(Collectors.toList());
//  }
//
//  public List<AccountRes> getAccounts(InternalAccountBalanceReq internalAccountBalanceReq) {
//    List<Account> accounts =
//        accountRepository.findAllByAccountUserIdAndAccountUserType(
//            internalAccountBalanceReq.getUserId(),
//            internalAccountBalanceReq.getUserType());
//    return accounts.stream()
//        .map(AccountMapper.INSTANCE::toAccountRes)
//        .collect(Collectors.toList());
//  }
//
//  public Page<AccountEventDTO> accountHistory(AccountHistoryReq accountHistoryReq) {
//    Pageable pageable = PageRequest.of(accountHistoryReq.getPage(), accountHistoryReq.getSize());
//    Instant fromDate = accountHistoryReq.getFromDate().atStartOfDay().toInstant(ZoneOffset.UTC);
//    Instant toDate =
//        accountHistoryReq.getToDate().atStartOfDay().plusDays(1).toInstant(ZoneOffset.UTC);
//    Page<AccountEvent> accountEvents =
//        accountEventRepository.findAllByAccountUserIdAndUserTypeAndCurrencyCodeAndDate(
//            accountHistoryReq.getLoggedInUser().getUserId(),
//            accountHistoryReq.getLoggedInUser().getUserType(),
//            accountHistoryReq.getCurrencyCodes(),
//            fromDate,
//            toDate,
//            pageable);
//    return accountEvents.map(AccountEventMapper.INSTANCE::toAccountEventDto);
//  }
//
//  public Account getCommissionAccount(AccountTransaction accountTransaction) {
//
//    return accountRepository.findFirstByCurrencyCodeAndAccountUserTypeAndAccountUserId(
//        accountTransaction.getCurrencyCode(), UserType.ADMIN_ACCOUNT,
//        Constants.AdminAccountUserId.COMMISSION_ACCOUNT.toString());
//  }
//
//  public Account debitCommissionAccount(AccountTransaction accountTransaction, BigDecimal amount) {
//    return accountRepository.findFirstByCurrencyCodeAndAccountUserTypeAndAccountUserId(
//        accountTransaction.getCurrencyCode(), UserType.ADMIN_ACCOUNT,
//        Constants.AdminAccountUserId.FEE_ACCOUNT.toString());
//  }
//}
