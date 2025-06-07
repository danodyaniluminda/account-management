//package com.biapay.accountmanagement.service;
//
//import com.biapay.accountmanagement.domain.Account_;
//import com.biapay.accountmanagement.dto.AccountReportDTO.AccountAggregateByCurrencyDto;
//import com.biapay.accountmanagement.dto.AccountReportDTO.AccountAggregateByUserTypeCurrencyDto;
//import com.biapay.accountmanagement.dto.AccountReportDTO.AccountAggregateByUserTypeDto;
//import com.biapay.accountmanagement.dto.AccountReportDTO.AccountAggregateByAccountTypeDto;
//import com.biapay.core.model.Account;
//import java.time.LocalDate;
//import java.time.ZoneId;
//import java.util.ArrayList;
//import java.util.List;
//import javax.persistence.EntityManager;
//import javax.persistence.Tuple;
//import javax.persistence.criteria.CriteriaBuilder;
//import javax.persistence.criteria.CriteriaQuery;
//import javax.persistence.criteria.Predicate;
//import javax.persistence.criteria.Root;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//@Service
//@Slf4j
//public class AccountReportService {
//
//  @Autowired
//  private EntityManager entityManager;
//
//  public List<AccountAggregateByUserTypeDto> getTotalBalanceByUserType(String userTypeFilter) {
//    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
//    CriteriaQuery<Tuple> cq = cb.createTupleQuery();
//    Root<Account> root = cq.from(Account.class);
//
//    List<Predicate> conditionsList = new ArrayList<>();
//
//    if (userTypeFilter != null) {
//      conditionsList.add(cb.equal(root.get("accountUserType"), userTypeFilter));
//    }
//
//    cq.multiselect(
//            cb.sum(root.get("balance")),
//            root.get("accountUserType")
//        )
//        .where(conditionsList.toArray(new Predicate[]{}))
//        .groupBy(
//            root.get(Account_.ACCOUNT_USER_TYPE)
//        )
//        .orderBy(cb.asc(root.get("accountUserType")));
//
//    List<Tuple> resultList = entityManager.createQuery(cq).getResultList();
//
//    List<AccountAggregateByUserTypeDto> results = new ArrayList<>(0);
//    for (Tuple result : resultList) {
//      AccountAggregateByUserTypeDto dto = AccountAggregateByUserTypeDto.builder()
//          .balance(Double.valueOf(result.get(0).toString()))
//          .accountUserType(result.get(1).toString())
//          .build();
//
//      results.add(dto);
//    }
//
//    return results;
//  }
//
//  public List<AccountAggregateByAccountTypeDto> getTotalBalanceByAccountType(String accountTypeFilter) {
//    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
//    CriteriaQuery<Tuple> cq = cb.createTupleQuery();
//    Root<Account> root = cq.from(Account.class);
//
//    List<Predicate> conditionsList = new ArrayList<>();
//
//    if (accountTypeFilter != null) {
//      conditionsList.add(cb.equal(root.get("accountType").as(String.class), accountTypeFilter));
//    }
//
//    cq.multiselect(
//            cb.sum(root.get("balance")),
//            root.get("accountType").as(String.class)
//        )
//        .where(conditionsList.toArray(new Predicate[]{}))
//        .groupBy(
//            root.get(Account_.ACCOUNT_TYPE)
//        )
//        .orderBy(cb.asc(root.get("accountType").as(String.class)));
//
//    List<Tuple> resultList = entityManager.createQuery(cq).getResultList();
//
//    List<AccountAggregateByAccountTypeDto> results = new ArrayList<>(0);
//    for (Tuple result : resultList) {
//      AccountAggregateByAccountTypeDto dto = AccountAggregateByAccountTypeDto.builder()
//          .balance(Double.valueOf(result.get(0).toString()))
//          .accountType(result.get(1).toString())
//          .build();
//
//      results.add(dto);
//    }
//
//    return results;
//  }
//
//  public List<AccountAggregateByUserTypeCurrencyDto> totalBalanceHistory(
//      String userTypeFilter,
//      String accountTypeFilter,
//      String currencyFilter,
//      String fromDateFilter,
//      String toDateFilter
//  ) {
//    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
//    CriteriaQuery<Tuple> cq = cb.createTupleQuery();
//    Root<Account> root = cq.from(Account.class);
//
//    List<Predicate> conditionsList = new ArrayList<>();
//
//    LocalDate today = LocalDate.now();
//    ZoneId z = ZoneId.systemDefault();
//    LocalDate fromDate = null;
//    LocalDate toDate = null;
//
//    if (fromDateFilter == null) {
//      fromDate = today;
//    } else {
//      fromDate = LocalDate.parse(fromDateFilter);
//    }
//    if (toDateFilter == null) {
//      toDate = today.plusDays(30);
//    } else {
//      toDate = LocalDate.parse(toDateFilter);
//    }
//
//    conditionsList.add(cb.between(root.get("createdDate").as(LocalDate.class), fromDate, toDate));
//
//    if (userTypeFilter != null) {
//      conditionsList.add(cb.equal(root.get("accountUserType").as(String.class), userTypeFilter));
//    }
//
//    if (accountTypeFilter != null) {
//      conditionsList.add(cb.equal(root.get("accountType").as(String.class), accountTypeFilter));
//    }
//
//    if (currencyFilter != null) {
//      conditionsList.add(cb.equal(root.get("currencyCode"), currencyFilter));
//    }
//
//    cq.multiselect(
//            cb.sum(root.get("balance")),
//            root.get("accountUserType").as(String.class),
//            root.get("accountType").as(String.class),
//            root.get("currencyCode").as(String.class),
//            root.get("createdDate").as(LocalDate.class)
//        )
//        .where(conditionsList.toArray(new Predicate[]{}))
//        .groupBy(
//            root.get(Account_.ACCOUNT_USER_TYPE),
//            root.get(Account_.ACCOUNT_TYPE),
//            root.get(Account_.CURRENCY_CODE),
//            root.get("createdDate").as(LocalDate.class)
//        )
//        .orderBy(cb.asc(root.get("createdDate").as(LocalDate.class)));
//
//    List<Tuple> resultList = entityManager.createQuery(cq).getResultList();
//
//    List<AccountAggregateByUserTypeCurrencyDto> results = new ArrayList<>(0);
//    for (Tuple result : resultList) {
//      AccountAggregateByUserTypeCurrencyDto dto = AccountAggregateByUserTypeCurrencyDto.builder()
//          .balance(Double.valueOf(result.get(0).toString()))
//          .accountUserType(result.get(1).toString())
//          .accountType(result.get(2).toString())
//          .currency(result.get(3).toString())
//          .date(result.get(4).toString())
//          .build();
//
//      results.add(dto);
//    }
//
//    return results;
//  }
//
//  public List<AccountAggregateByCurrencyDto> totalBalanceWithByCurrency(
//      String currencyFilter,
//      String fromDateFilter,
//      String toDateFilter
//  ) {
//    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
//    CriteriaQuery<Tuple> cq = cb.createTupleQuery();
//    Root<Account> root = cq.from(Account.class);
//
//    List<Predicate> conditionsList = new ArrayList<>();
//
//    LocalDate today = LocalDate.now();
//    ZoneId z = ZoneId.systemDefault();
//    LocalDate fromDate = null;
//    LocalDate toDate = null;
//
//    if (fromDateFilter == null) {
//      fromDate = today;
//    } else {
//      fromDate = LocalDate.parse(fromDateFilter);
//    }
//    if (toDateFilter == null) {
//      toDate = today.plusDays(30);
//    } else {
//      toDate = LocalDate.parse(toDateFilter);
//    }
//
//    conditionsList.add(cb.between(root.get("createdDate").as(LocalDate.class), fromDate, toDate));
//
//    if (currencyFilter != null) {
//      conditionsList.add(cb.equal(root.get("currencyCode"), currencyFilter));
//    }
//
//    cq.multiselect(
//            cb.sum(root.get("balance")),
//            root.get("currencyCode").as(String.class),
//            root.get("createdDate").as(LocalDate.class)
//        )
//        .where(conditionsList.toArray(new Predicate[]{}))
//        .groupBy(
//            root.get(Account_.CURRENCY_CODE),
//            root.get("createdDate").as(LocalDate.class)
//        )
//        .orderBy(cb.asc(root.get("createdDate").as(LocalDate.class)));
//
//    List<Tuple> resultList = entityManager.createQuery(cq).getResultList();
//
//    List<AccountAggregateByCurrencyDto> results = new ArrayList<>(0);
//    for (Tuple result : resultList) {
//      AccountAggregateByCurrencyDto dto = AccountAggregateByCurrencyDto.builder()
//          .balance(Double.valueOf(result.get(0).toString()))
//          .currency(result.get(1).toString())
//          .date(result.get(2).toString())
//          .build();
//
//      results.add(dto);
//    }
//
//    return results;
//  }
//
//  public Object totalBalancePerCurrencyForTop(String userTypeFilter, String currencyFilter,
//      String fromDateFilter, String toDateFilter) {
//    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
//    CriteriaQuery<Tuple> cq = cb.createTupleQuery();
//    Root<Account> root = cq.from(Account.class);
//
//    List<Predicate> conditionsList = new ArrayList<>();
//
//    LocalDate today = LocalDate.now();
//    ZoneId z = ZoneId.systemDefault();
//    LocalDate fromDate = null;
//    LocalDate toDate = null;
//
//    if (fromDateFilter == null) {
//      fromDate = today;
//    } else {
//      fromDate = LocalDate.parse(fromDateFilter);
//    }
//    if (toDateFilter == null) {
//      toDate = today.plusDays(30);
//    } else {
//      toDate = LocalDate.parse(toDateFilter);
//    }
//
//    conditionsList.add(cb.between(root.get("createdDate").as(LocalDate.class), fromDate, toDate));
//
//    if (currencyFilter != null) {
//      conditionsList.add(cb.equal(root.get("currencyCode"), currencyFilter));
//    }
//
//    cq.multiselect(
//            cb.sum(root.get("balance")),
//            root.get("currencyCode").as(String.class),
//            root.get("createdDate").as(LocalDate.class)
//        )
//        .where(conditionsList.toArray(new Predicate[]{}))
//        .groupBy(
//            root.get(Account_.CURRENCY_CODE),
//            root.get("createdDate").as(LocalDate.class)
//        )
//        .orderBy(cb.asc(root.get("createdDate").as(LocalDate.class)));
//
//    List<Tuple> resultList = entityManager.createQuery(cq).getResultList();
//
//    List<AccountAggregateByCurrencyDto> results = new ArrayList<>(0);
//    for (Tuple result : resultList) {
//      AccountAggregateByCurrencyDto dto = AccountAggregateByCurrencyDto.builder()
//          .balance(Double.valueOf(result.get(0).toString()))
//          .currency(result.get(1).toString())
//          .date(result.get(2).toString())
//          .build();
//
//      results.add(dto);
//    }
//
//    return results;
//  }
//}
