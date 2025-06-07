//package com.biapay.accountmanagement.service;
//
//import com.biapay.accountmanagement.config.APIError;
//import com.biapay.accountmanagement.exception.BIABadRequestException;
//import com.biapay.accountmanagement.repository.AccountTransactionRepository;
//import com.biapay.core.dto.CustomerDTO;
//import com.biapay.core.model.UserType;
//import java.math.BigDecimal;
//import java.time.LocalDate;
//import java.time.ZoneId;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//
//@Service
//@Slf4j
//public class AccountRuleService {
//
//  private final CustomerManagementAPI customerManagementAPI;
//  private final BackofficeTransactionLimitServiceAPI backofficeTransactionLimitServiceAPI;
//  private final AccountTransactionRepository accountTransactionRepository;
//  private final String activeCustomerSubscriptionPlanId;
//  private final String inactiveCustomerSubscriptionPlanId;
//
//  public AccountRuleService(
//      CustomerManagementAPI customerManagementAPI,
//      BackofficeTransactionLimitServiceAPI backofficeTransactionLimitServiceAPI,
//      AccountTransactionRepository accountTransactionRepository,
//      @Value("${account.limit.customer.active}") String activeCustomerSubscriptionPlanId,
//      @Value("${account.limit.customer.inActive}") String inactiveCustomerSubscriptionPlanId) {
//    this.customerManagementAPI = customerManagementAPI;
//    this.backofficeTransactionLimitServiceAPI = backofficeTransactionLimitServiceAPI;
//    this.accountTransactionRepository = accountTransactionRepository;
//    this.activeCustomerSubscriptionPlanId = activeCustomerSubscriptionPlanId;
//    this.inactiveCustomerSubscriptionPlanId = inactiveCustomerSubscriptionPlanId;
//  }
//
//  public void ensureRule(
//      UserType userType, String userId, String currencyCode, BigDecimal amount) {
//    try {
//      if (userType == UserType.CUSTOMER) {
//        doCustomerLimitCheck(userId, amount);
//      }
//    } catch (Exception e) {
//      log.error(e.getMessage(), e);
//      throw new BIABadRequestException(APIError.ACCOUNT_LIMIT_CHECK_FAILED.getDescription());
//    }
//  }
//
//  private void doCustomerLimitCheck(String userId, BigDecimal amount) {
//    CustomerDTO customerDTO = customerManagementAPI.getCustomerByCustomerId(userId);
//    Long todayCount =
//        accountTransactionRepository.todayTransactionCount(
//            LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant(),
//            LocalDate.now()
//                .plusDays(1)
//                .atStartOfDay(ZoneId.systemDefault())
//                .toInstant(),
//            customerDTO.getCustomerId().toString(),
//            UserType.CUSTOMER);
//    BigDecimal todayAmount =
//        accountTransactionRepository.todayTransactionAmount(
//            LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant(),
//            LocalDate.now()
//                .plusDays(1)
//                .atStartOfDay(ZoneId.systemDefault())
//                .toInstant(),
//            customerDTO.getCustomerId().toString(),
//            UserType.CUSTOMER);
//    log.info("Today transaction count: {}", todayCount);
//    log.info("Today transaction amount: {}", todayAmount);
//
//    BackofficeTransactionLimitServiceAPI.AccountTransactionLimit accountTransactionLimit = null;
//    if (customerDTO.getStatus() == CustomerStatus.ACTIVE) {
//      accountTransactionLimit =
//          backofficeTransactionLimitServiceAPI.getTransactionLimitCustomer(
//              Long.valueOf(activeCustomerSubscriptionPlanId));
//    } else {
//      accountTransactionLimit =
//          backofficeTransactionLimitServiceAPI.getTransactionLimitCustomer(
//              Long.valueOf(inactiveCustomerSubscriptionPlanId));
//    }
//    if (accountTransactionLimit != null) {
//      log.info(
//          "Daily transaction number: {}",
//          accountTransactionLimit.getDailyNumberOfTransaction());
//      log.info(
//          "Daily transaction amount: {}",
//          accountTransactionLimit.getDailyAmountOfTransaction());
//
//      if (accountTransactionLimit.getDailyAmountOfTransaction() != null) {
//        if (todayAmount
//            .add(amount)
//            .compareTo(accountTransactionLimit.getDailyAmountOfTransaction())
//            > 0) {
//          throw new BIABadRequestException(
//              APIError.ACCOUNT_TRANSACTION_LIMIT_EXCEEDED.getDescription());
//        }
//      }
//      if (todayCount + 1 > accountTransactionLimit.getDailyNumberOfTransaction()) {
//        throw new BIABadRequestException(
//            APIError.ACCOUNT_TRANSACTION_LIMIT_EXCEEDED.getDescription());
//      }
//    }
//  }
//}
