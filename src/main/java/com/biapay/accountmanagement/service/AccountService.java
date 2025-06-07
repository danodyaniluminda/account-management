package com.biapay.accountmanagement.service;

import com.biapay.accountmanagement.config.APIError;
import com.biapay.accountmanagement.config.Constants;
import com.biapay.accountmanagement.dto.AccountCreateReq;
import com.biapay.accountmanagement.dto.SettlementAccountCreateReq;
import com.biapay.accountmanagement.dto.response.ResponseCode;
import com.biapay.accountmanagement.dto.response.ResponseDto;
import com.biapay.accountmanagement.exception.BIABadRequestException;
import com.biapay.accountmanagement.mapper.AccountMapper;
import com.biapay.core.constant.enums.AccountStatus;
import com.biapay.core.dto.LoggedInUserDTO;
import com.biapay.core.exception.BIAPayRuntimeException;
import com.biapay.core.exception.NotFoundException;
import com.biapay.core.model.Account;
import com.biapay.core.model.AccountEvent;
import com.biapay.core.model.Currency;
import com.biapay.core.model.Settlement;
import com.biapay.core.model.User;
import com.biapay.core.model.UserType;
import com.biapay.core.model.enums.AccountSubType;
import com.biapay.core.model.enums.AccountType;
import com.biapay.core.model.enums.SettlementStatus;
import com.biapay.core.repository.AccountEventRepository;
import com.biapay.core.repository.AccountRepository;
import com.biapay.core.repository.CurrencyRepository;
import com.biapay.core.repository.MerchantPOSRepository;
import com.biapay.core.repository.PaymentMethodRepository;
import com.biapay.core.repository.SettlementRepository;
import com.biapay.core.repository.UserRepository;
import com.biapay.core.util.AuthUtil;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@AllArgsConstructor
public class AccountService {

  private final CurrencyRepository currencyRepository;
  private final AccountRepository accountRepository;
  private final AccountEventRepository accountEventRepository;
  private final EntityManager entityManager;
  private final UserRepository userRepository;
  private final CriteriaBuilder criteriaBuilder;
  private final AuthUtil authUtil;
  private final PaymentMethodRepository paymentMethodRepository;
  private final SettlementRepository settlementRepository;
  private AccountMapper accountMapper;
  private final MerchantPOSRepository merchantPOSRepository;

  public List<Account> findAccountByAccountUserTypeAndAccountUserId(Long userId,
      UserType userType) {
    return accountRepository.findAllByAccountUserIdAndAccountUserType(userId, userType);
  }

  public Account findAccountByUserTypeAndUserIdAndCurrencyCode(UserType userType, Long userId,
      String currencyCode) {
    return accountRepository.findFirstByCurrencyCodeAndAccountUserTypeAndAccountUserId(currencyCode,
        userType, userId);
  }

  public boolean isAccountExist(UserType userType, Long userId, String currencyCode) {
    return
        accountRepository.existsByCurrencyCodeAndAccountUserTypeAndAccountUserId(currencyCode,
            userType, userId);
  }

  public boolean isAccountExist(AccountType accountType, AccountSubType accountSubType, Long userId, String currencyCode,
      Long paymentMethodId, Long settlementMethodId) {
    if(accountSubType != null) {
      if(paymentMethodId != null) {
        return accountRepository.existsByCurrencyCodeAndAccountTypeAndAccountSubTypeAndAccountUserIdAndPaymentMethodId(
            currencyCode, accountType, accountSubType, userId, paymentMethodId);
      } else if(settlementMethodId != null) {
        return accountRepository.existsByCurrencyCodeAndAccountTypeAndAccountSubTypeAndAccountUserIdAndSettlementMethodId(
            currencyCode, accountType, accountSubType, userId, settlementMethodId);
      } else {
        return accountRepository.existsByCurrencyCodeAndAccountTypeAndAccountSubTypeAndAccountUserId(
            currencyCode, accountType, accountSubType, userId);
      }
    } else {
      return
          accountRepository.existsByCurrencyCodeAndAccountTypeAndAccountUserId(currencyCode,
              accountType, userId);
    }
  }

  public void validateAccountExist(UserType userType, Long userId, String currencyCode) {
    if (!isAccountExist(userType, userId, currencyCode)) {
      throw new BIABadRequestException(APIError.ACCOUNT_NOT_FOUND.getDescription());
    }
  }

  @Transactional
  public void createAccountByUser(AccountCreateReq accountCreateRequest) {
    Optional<User> userOptional = userRepository.findByUserId(
        accountCreateRequest.getAccountUserId());

    if (userOptional.isPresent()) {

      List<AccountCreateReq> accountCreateReqs = new ArrayList<>();

      AccountType accountType;
      if (accountCreateRequest.getAccountType() == null) {
        switch (userOptional.get().getUserType()) {
          case USER:
            accountType = AccountType.USER;
            break;

          case MERCHANT:
            accountType = AccountType.MERCHANT;
            break;

          case AGENT:
            accountType = AccountType.AGENT;
            break;

          case ADMIN:
            accountType = AccountType.LEDGER;
            break;

          default:
            accountType = AccountType.REGULAR;
        }
      } else {
        accountType = accountCreateRequest.getAccountType();
      }

      if(accountCreateRequest.getCurrencyCode() == null) {
        prepareAccountCreateRequests(accountCreateReqs, userOptional.get(),
            accountCreateRequest.getCurrencyCode(), accountCreateRequest.getAccountUserId(),
            accountType, accountCreateRequest.getAccountSubType(),
            accountCreateRequest.getAccountStatus(),
            accountCreateRequest.getAccountNumber(), accountCreateRequest.getPaymentMethodId(),
            accountCreateRequest.getSettlementMethodId());
      }
      log.info("Assigned multiple records for account creation");

      for (AccountCreateReq accountCreateReq : accountCreateReqs) {
        if (!isAccountExist(accountCreateReq.getAccountType(), accountCreateReq.getAccountSubType(),
            accountCreateReq.getAccountUserId(), accountCreateReq.getCurrencyCode(),
            accountCreateReq.getPaymentMethodId(), accountCreateReq.getSettlementMethodId())) {
          createAccount(accountCreateReq);
        } else {
          log.info("Already account created for: {}", accountCreateReq.getAccountNumber());
        }
      }

      log.info("Processed account creation request for {}, currency {}",
          accountCreateRequest.getAccountUserId(), accountCreateRequest.getCurrencyCode());

    }
  }

  private void prepareAccountCreateRequests(List<AccountCreateReq> accountCreateReqs, User user,
      String currencyCode, Long accountUserId, AccountType accountType, AccountSubType accountSubType,
      AccountStatus accountStatus, String accountNumber, Long paymentMethodId, Long settlementMethodId) {

    if (currencyCode == null) {
      String accountNumberXAF = user.getMobileNumber() + "-" + "XAF" + "-" + accountUserId + "-" +
          user.getUserType();
      String accountNumberEUR = user.getMobileNumber() + "-" + "EUR" + "-" + accountUserId + "-" +
          user.getUserType();
      String accountNumberUSD = user.getMobileNumber() + "-" + "USD" + "-" + accountUserId + "-" +
          user.getUserType();

      accountCreateReqs.add(
          AccountCreateReq.builder()
              .accountType(accountType)
              .accountSubType(accountSubType)
              .accountNumber(accountNumber != null ? accountNumber : accountNumberXAF)
              .accountUserId(accountUserId)
              .currencyCode("XAF")
              .accountUserType(user.getUserType())
              .accountStatus(AccountStatus.ACTIVE)
              .paymentMethodId(paymentMethodId)
              .settlementMethodId(settlementMethodId)
              .build()
      );

      accountCreateReqs.add(
          AccountCreateReq.builder()
              .accountType(accountType)
              .accountSubType(accountSubType)
              .accountNumber(accountNumber != null ? accountNumber : accountNumberEUR)
              .accountUserId(accountUserId)
              .currencyCode("EUR")
              .accountUserType(user.getUserType())
              .accountStatus(AccountStatus.ACTIVE)
              .paymentMethodId(paymentMethodId)
              .settlementMethodId(settlementMethodId)
              .build()
      );

      accountCreateReqs.add(
          AccountCreateReq.builder()
              .accountType(accountType)
              .accountSubType(accountSubType)
              .accountNumber(accountNumber != null ? accountNumber : accountNumberUSD)
              .accountUserId(accountUserId)
              .currencyCode("USD")
              .accountUserType(user.getUserType())
              .accountStatus(AccountStatus.ACTIVE)
              .paymentMethodId(paymentMethodId)
              .settlementMethodId(settlementMethodId)
              .build()
      );
    } else {
      if(accountNumber != null) {
        accountNumber = user.getMobileNumber() + "-" + currencyCode + "-" + accountUserId + "-"
            + user.getUserType();
      }
      accountCreateReqs.add(
          AccountCreateReq.builder()
              .accountType(accountType)
              .accountSubType(accountSubType)
              .accountNumber(accountNumber)
              .accountUserId(accountUserId)
              .currencyCode(currencyCode)
              .accountUserType(user.getUserType())
              .accountStatus(accountStatus)
              .paymentMethodId(paymentMethodId)
              .settlementMethodId(settlementMethodId)
              .build()
      );
    }
  }

  public void createAccount(AccountCreateReq accountCreateReq) {
    log.info("validateAccountCreation for {}", accountCreateReq.getAccountNumber());
    validateAccountCreation(accountCreateReq);
    try {

      Optional<Currency> currency = currencyRepository.findByCode(
          accountCreateReq.getCurrencyCode());
      if (currency.isPresent()) {
        Account account =
            Account.builder()
                .balance(BigDecimal.ZERO)
                .accountNumber(accountCreateReq.getAccountNumber())
                .currencyCode(currency.get().getCode())
                .accountUserType(accountCreateReq.getAccountUserType())
                .accountUserId(accountCreateReq.getAccountUserId())
                .accountType(accountCreateReq.getAccountType())
                .accountSubType(accountCreateReq.getAccountSubType())
                .accountStatus(accountCreateReq.getAccountStatus())
                .paymentMethodId(accountCreateReq.getPaymentMethodId())
                .settlementMethodId(accountCreateReq.getSettlementMethodId())
                .build();
        accountRepository.save(account);
      }
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
  }

  public void validateAccountCreation(AccountCreateReq accountCreateReq) {
    boolean isExists;

    if (accountCreateReq.getSettlementMethodId() != null) {
      isExists = accountCreateReq.getAccountSubType() != null
          ? accountRepository.existsByCurrencyCodeAndAccountTypeAndAccountSubTypeAndAccountUserIdAndSettlementMethodId(
          accountCreateReq.getCurrencyCode(),
          accountCreateReq.getAccountType(),
          accountCreateReq.getAccountSubType(),
          accountCreateReq.getAccountUserId(),
          accountCreateReq.getSettlementMethodId())
          : accountRepository.existsByCurrencyCodeAndAccountTypeAndAccountUserIdAndSettlementMethodId(
              accountCreateReq.getCurrencyCode(),
              accountCreateReq.getAccountType(),
              accountCreateReq.getAccountUserId(),
              accountCreateReq.getSettlementMethodId());
    } else if (accountCreateReq.getPaymentMethodId() != null) {
      isExists = accountCreateReq.getAccountSubType() != null
          ? accountRepository.existsByCurrencyCodeAndAccountTypeAndAccountSubTypeAndAccountUserIdAndPaymentMethodId(
          accountCreateReq.getCurrencyCode(),
          accountCreateReq.getAccountType(),
          accountCreateReq.getAccountSubType(),
          accountCreateReq.getAccountUserId(),
          accountCreateReq.getPaymentMethodId())
          : accountRepository.existsByCurrencyCodeAndAccountTypeAndAccountUserIdAndPaymentMethodId(
              accountCreateReq.getCurrencyCode(),
              accountCreateReq.getAccountType(),
              accountCreateReq.getAccountUserId(),
              accountCreateReq.getPaymentMethodId());
    } else {
      isExists = accountCreateReq.getAccountSubType() != null
          ? accountRepository.existsByCurrencyCodeAndAccountTypeAndAccountSubTypeAndAccountUserId(
          accountCreateReq.getCurrencyCode(),
          accountCreateReq.getAccountType(),
          accountCreateReq.getAccountSubType(),
          accountCreateReq.getAccountUserId())
          : accountRepository.existsByCurrencyCodeAndAccountTypeAndAccountUserId(
              accountCreateReq.getCurrencyCode(),
              accountCreateReq.getAccountType(),
              accountCreateReq.getAccountUserId());
    }

    if (isExists) {
      throw new BIABadRequestException(APIError.ACCOUNT_ALREADY_EXISTS.getDescription());
    }
  }

  public ResponseDto<Object> getAccountByNumber(String accountNumber) {
    ResponseDto<Object> response = new ResponseDto<>();

    Optional<List<Account>> accountOptional = accountRepository.findAllByAccountNumber(
        accountNumber);

    if (accountOptional.isPresent()) {
      response.setMessage(ResponseCode.SUCCESS.getMessage());
      response.setCode(ResponseCode.SUCCESS.getCode());
      response.setData(accountOptional.get().stream().map(accountMapper::toDto));

      return response;
    } else {
      response.setCode(ResponseCode.NOT_FOUND.getCode());
      response.setMessage(ResponseCode.NOT_FOUND.getMessage());
      response.setError("No account found!");

      return response;
    }
  }

  public Account getAccount(Long userId, AccountType accountType, String currencyCode) {
    try {
      return accountRepository.findAccountForUpdate(userId, currencyCode, accountType)
          .orElseThrow(() -> new NotFoundException(
              "No account found with currency: " + currencyCode + " for userId:"
                  + userId + " for account type:" + accountType));
    } catch (Exception e) {
      log.error("error: ", e);
      throw new BIAPayRuntimeException("Error occurred on fetching account {}", e);
    }
  }

  public Account getMerchantAccount(Long userId, AccountType accountType, String currencyCode, Long posId) {
    try {
      Account createdAccount = null;
      if(!accountRepository.findByAccountUserIdAndAccountTypeAndCurrencyCodeAndPosId(userId,
          accountType, currencyCode, posId).isPresent()) {
        // create account
        createdAccount = createMerchantAccount(userId, currencyCode, posId);
      }
      return accountRepository.findAccountForUpdate(userId, currencyCode, accountType, posId)
          .orElseThrow(() -> new NotFoundException(
              "No account found with currency: " + currencyCode + " for userId:"
                  + userId + " for account type:" + accountType));
    } catch (Exception e) {
      log.error("error: ", e);
      throw new BIAPayRuntimeException("Error occurred on fetching account {}", e);
    }
  }

  private Account createMerchantAccount(Long userId, String currencyCode, Long posId) {
    Account account = new Account();
    account.setCurrencyCode(currencyCode);
    account.setAccountUserId(userId);
    account.setAccountUserType(UserType.MERCHANT);
    account.setAccountType(AccountType.MERCHANT);
    account.setBalance(BigDecimal.ZERO);
    account.setOnHold(BigDecimal.ZERO);
    account.setAccountStatus(AccountStatus.ACTIVE);
    account.setPosId(posId);
    return accountRepository.save(account);
  }

  public Account getAccount(Long userId, AccountType accountType, AccountSubType accountSubType,
      String currencyCode) {
    try {
      return accountRepository.findAccountForUpdate(userId, currencyCode, accountType, accountSubType)
          .orElseThrow(() -> new NotFoundException(
              "No account found with currency: " + currencyCode + " for userId:"
                  + userId + " for account type:" + accountType));
    } catch (Exception e) {
      log.error("error: ", e);
      throw new BIAPayRuntimeException("Error occurred on fetching account {}", e);
    }
  }

  public Account getSettlementAccount(AccountSubType accountSubType, String currencyCode,
      Long settlementMethodId, Long paymentMethodId) {
    if (accountSubType == null) {
      throw new BIAPayRuntimeException("Account subtype not present");
    }

    try {
      switch (accountSubType) {
        case COLLECTION:
          return accountRepository.findSettlementCollectionAccountForUpdate(currencyCode, paymentMethodId)
              .orElseThrow(() -> new NotFoundException(
                  "No account found with currency: " + currencyCode + " for payment method id: " + paymentMethodId));

        case DISBURSEMENT:
          return accountRepository.findSettlementDisbursementAccountForUpdate(currencyCode, settlementMethodId)
              .orElseThrow(() -> new NotFoundException(
                  "No account found with currency: " + currencyCode + " for settlement method id: " + settlementMethodId));

        default:
          throw new BIAPayRuntimeException("Invalid account subtype: " + accountSubType);
      }
    } catch (Exception e) {
      log.error("Error fetching settlement account: ", e);
      throw new BIAPayRuntimeException("Error occurred while fetching settlement account", e);
    }
  }

  public Account getPSPFeeAccount(AccountSubType accountSubType, String currencyCode,
      Long settlementMethodId, Long paymentMethodId) {
    if (accountSubType == null) {
      throw new BIAPayRuntimeException("Account subtype not present");
    }

    try {
      switch (accountSubType) {
        case COLLECTION:
          return accountRepository.findPSPCollectionAccountForUpdate(currencyCode, paymentMethodId)
              .orElseThrow(() -> new NotFoundException(
                  "No account found with currency: " + currencyCode + " for payment method id: " + paymentMethodId));

        case DISBURSEMENT:
          return accountRepository.findPSPDisbursementAccountForUpdate(currencyCode, settlementMethodId)
              .orElseThrow(() -> new NotFoundException(
                  "No account found with currency: " + currencyCode + " for settlement method id: " + settlementMethodId));

        default:
          throw new BIAPayRuntimeException("Invalid account subtype: " + accountSubType);
      }
    } catch (Exception e) {
      log.error("Error fetching settlement account: ", e);
      throw new BIAPayRuntimeException("Error occurred while fetching settlement account", e);
    }
  }

  public Optional<Account> getLedgerAccount(String currencyCode) {
    return accountRepository
        .findFirstByCurrencyCodeAndAccountType(
            Objects.requireNonNullElse(currencyCode, Constants.DEFAULT_CURRENCY_CODE),
            AccountType.LEDGER);

  }

  public ResponseDto<Object> getAccounts(HashMap<String, String> filters) {
    ResponseDto<Object> response = new ResponseDto<>();

    CriteriaQuery<Account> query = criteriaBuilder.createQuery(Account.class);
    Root<Account> root = query.from(Account.class);

    // Create predicates based on filters
    List<Predicate> predicates = new ArrayList<>();

    if (authUtil.getLoggedInUserInfo().getUserType() == UserType.ADMIN) {
      if (filters.get("userId") != null) {
        predicates.add(criteriaBuilder.equal(root.get("accountUserId"), filters.get("userId")));
      } else {
        throw new BIABadRequestException("Please provide user Id");
      }
    } else {
      predicates.add(
          criteriaBuilder.equal(root.get("accountUserId"), authUtil.getUser().getUserId()));
    }

    if (filters.get("userType") != null) {
      predicates.add(criteriaBuilder.equal(root.get("accountUserType"),
          UserType.valueOf(filters.get("userType"))));
    }
    if (filters.get("currency") != null) {
      predicates.add(criteriaBuilder.equal(root.get("currencyCode"), filters.get("currency")));
    }
    if (filters.get("accountNumber") != null) {
      predicates.add(
          criteriaBuilder.equal(root.get("accountNumber"), filters.get("accountNumber")));
    }

    if (filters.get("fromDate") != null && filters.get("toDate") != null) {
      LocalDateTime fromDate = LocalDate.parse(filters.get("fromDate")).atStartOfDay();
      LocalDateTime toDate = LocalDate.parse(filters.get("toDate")).atTime(LocalTime.MAX);
      predicates.add(criteriaBuilder.between(root.get("createdDate"), fromDate, toDate));
    }

    if (filters.get("fromDate") != null && filters.get("toDate") == null) {
      LocalDateTime fromDate = LocalDate.parse(filters.get("fromDate")).atStartOfDay();
      predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdDate"), fromDate));
    }

    if (filters.get("fromDate") == null && filters.get("toDate") != null) {
      LocalDateTime toDate = LocalDate.parse(filters.get("toDate")).atTime(LocalTime.MAX);
      predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdDate"), toDate));
    }

    Order sorting = criteriaBuilder.desc(root.get("createdDate"));
    //    sorting
    if (filters.get("sortBy") != null) {
      if (filters.get("sortOrder") != null) {
        if (filters.get("sortOrder").equalsIgnoreCase("ASC")) {
          sorting = criteriaBuilder.asc(root.get(filters.get("sortBy")));
        }
        if (filters.get("sortOrder").equalsIgnoreCase("DESC")) {
          sorting = criteriaBuilder.desc(root.get(filters.get("sortBy")));
        }
      }
    }
    query.orderBy(sorting);

    // Apply the predicates to the query
    query.where(predicates.toArray(new Predicate[0]));
    // Execute the query and return the results
    List<Account> accounts = entityManager.createQuery(query).getResultList();

    if (accounts.isEmpty()) {
      response.setCode(ResponseCode.NOT_FOUND.getCode());
      response.setMessage(ResponseCode.NOT_FOUND.getMessage());
      response.setError("No records!");
      response.setSuccess(false);

      return response;
    } else {
      response.setMessage(ResponseCode.SUCCESS.getMessage());
      response.setCode(ResponseCode.SUCCESS.getCode());
      response.setData(accounts.stream()
          .map(AccountMapper.INSTANCE::toAccountRes)
          .collect(Collectors.toList()));
      response.setSuccess(true);

      return response;
    }
  }

  public Page<AccountEvent> accountHistory(Long userId, String fromDate, String toDate, String currency, int page, int size) {
    Pageable pageable = PageRequest.of(page, size);
    Instant startDate = LocalDate.parse(fromDate).atStartOfDay().toInstant(ZoneOffset.UTC);
    Instant endDate = LocalDate.parse(toDate).atTime(LocalTime.MAX).toInstant(ZoneOffset.UTC);
    Page<AccountEvent> accountEvents = null;
    LoggedInUserDTO loggedInUser = AuthUtil.getLoggedInUser();

    if (loggedInUser.getUserType().equals(UserType.ADMIN)) {
      if (userId == null) {
        accountEvents =
            accountEventRepository.findAllByDateAndCurrency(
                startDate,
                endDate,
                currency,
                pageable);
      } else {
        accountEvents =
            accountEventRepository.findAllByAccountUserIdAndDateAndCurrency(
                userId,
                startDate,
                endDate,
                currency,
                pageable);
      }

    } else {
      accountEvents =
          accountEventRepository.findAllByAccountUserIdAndDateAndCurrency(
              loggedInUser.getUserId(),
              startDate,
              endDate,
              currency,
              pageable);
    }
    return accountEvents;
  }

  public void createSettlementAccountByUser(SettlementAccountCreateReq accountCreateReq) {
    userRepository.findByUserId(accountCreateReq.getAccountUserId()).ifPresent(user -> {
      List<String> currencies = List.of("XAF", "USD", "EUR");

      boolean isDisbursement = AccountSubType.DISBURSEMENT.equals(accountCreateReq.getAccountSubType());
      boolean isCollection = AccountSubType.COLLECTION.equals(accountCreateReq.getAccountSubType());

      for (String currency : currencies) {
        boolean accountExists = isDisbursement
            ? accountRepository.existsByCurrencyCodeAndAccountTypeAndAccountSubTypeAndSettlementMethodId(
            currency, AccountType.SETTLEMENT, accountCreateReq.getAccountSubType(), accountCreateReq.getSettlementMethodId())
            : accountRepository.existsByCurrencyCodeAndAccountTypeAndAccountSubTypeAndPaymentMethodId(
                currency, AccountType.SETTLEMENT, accountCreateReq.getAccountSubType(), accountCreateReq.getPaymentMethodId());

        if ((isDisbursement && accountCreateReq.getSettlementMethodId() != null ||
            isCollection && accountCreateReq.getPaymentMethodId() != null) && !accountExists) {

          AccountCreateReq.AccountCreateReqBuilder builder = AccountCreateReq.builder()
              .accountNumber(accountCreateReq.getAccountNumber())
              .accountStatus(AccountStatus.ACTIVE)
              .accountType(AccountType.SETTLEMENT)
              .accountSubType(accountCreateReq.getAccountSubType())
              .accountUserId(accountCreateReq.getAccountUserId())
              .accountUserType(user.getUserType())
              .balance(BigDecimal.ZERO)
              .currencyCode(currency);

          if (isDisbursement) {
            builder.settlementMethodId(accountCreateReq.getSettlementMethodId());
          } else {
            builder.paymentMethodId(accountCreateReq.getPaymentMethodId());
          }

          createAccount(builder.build());
        }
      }
    });
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void markSettlementAsFailed(UUID clientTransactionId) {
    Settlement settlement = settlementRepository.findSettlementForUpdate(clientTransactionId);
    settlement.setStatus(SettlementStatus.FAILED);
    settlementRepository.save(settlement);
  }

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
}
