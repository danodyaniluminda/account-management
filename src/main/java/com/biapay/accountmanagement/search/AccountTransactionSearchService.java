package com.biapay.accountmanagement.search;

import com.biapay.accountmanagement.criteria.AccountTransactionSearchCriteria;
import com.biapay.core.model.AccountTransaction;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class AccountTransactionSearchService {

//  public Specification<AccountTransaction> buildCriteria(
//      AccountTransactionSearchCriteria accountTransactionSearchCriteria) {
//    List<Predicate> predicates = new ArrayList<>();
//    return (root, criteriaQuery, criteriaBuilder) -> {
//      if (accountTransactionSearchCriteria.getAccountTransactionId().getEquals() != null) {
//        predicates.add(
//            criteriaBuilder.equal(
//                root.get(AccountTransaction_.accountTransactionId),
//                accountTransactionSearchCriteria
//                    .getAccountTransactionId()
//                    .getEquals()));
//      }
//      if (accountTransactionSearchCriteria.getAccountTransactionId().getIn() != null
//          && accountTransactionSearchCriteria.getAccountTransactionId().getIn().size()
//          > 0) {
//        predicates.add(
//            root.get(AccountTransaction_.accountTransactionId)
//                .in(
//                    accountTransactionSearchCriteria
//                        .getAccountTransactionId()
//                        .getIn()));
//      }
//      if (accountTransactionSearchCriteria.getStatus().getEquals() != null) {
//        predicates.add(
//            criteriaBuilder.equal(
//                root.get(AccountTransaction_.accountTransactionStatus),
//                accountTransactionSearchCriteria.getStatus().getEquals()));
//      }
//      if (accountTransactionSearchCriteria.getStatus().getIn() != null
//          && accountTransactionSearchCriteria.getStatus().getIn().size() > 0) {
//        predicates.add(
//            root.get(AccountTransaction_.accountTransactionStatus)
//                .in(accountTransactionSearchCriteria.getStatus().getIn()));
//      }
//      if (accountTransactionSearchCriteria.getType().getEquals() != null) {
//        predicates.add(
//            criteriaBuilder.equal(
//                root.get(AccountTransaction_.accountTransactionType),
//                accountTransactionSearchCriteria.getType().getEquals()));
//      }
//      if (accountTransactionSearchCriteria.getType().getIn() != null
//          && accountTransactionSearchCriteria.getType().getIn().size() > 0) {
//        predicates.add(
//            root.get(AccountTransaction_.accountTransactionType)
//                .in(accountTransactionSearchCriteria.getType().getIn()));
//      }
//      if (accountTransactionSearchCriteria.getCreatedDate().getEquals() != null) {
//        predicates.add(
//            criteriaBuilder.equal(
//                root.get(AccountTransaction_.createdDate),
//                accountTransactionSearchCriteria.getCreatedDate().getEquals()));
//      }
//      if (accountTransactionSearchCriteria.getCreatedDate().getLessThan() != null) {
//        predicates.add(
//            criteriaBuilder.lessThanOrEqualTo(
//                root.get(AccountTransaction_.createdDate),
//                accountTransactionSearchCriteria
//                    .getCreatedDate()
//                    .getLessThan()
//                    .atStartOfDay(ZoneId.systemDefault())
//                    .toInstant()));
//      }
//      if (accountTransactionSearchCriteria.getCreatedDate().getGreaterThan() != null) {
//        predicates.add(
//            criteriaBuilder.greaterThanOrEqualTo(
//                root.get(AccountTransaction_.createdDate),
//                accountTransactionSearchCriteria
//                    .getCreatedDate()
//                    .getGreaterThan()
//                    .atStartOfDay(ZoneId.systemDefault())
//                    .toInstant()));
//      }
//
//      return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
//    };
//  }
}
