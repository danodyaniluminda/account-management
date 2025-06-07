package com.biapay.accountmanagement.criteria;

import com.biapay.core.constant.enums.AccountTransactionStatus;
import com.biapay.core.model.enums.AccountTransactionType;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountTransactionSearchCriteria {

  private LongFilter accountTransactionId = new LongFilter();
  private AccountTransactionStatusFilter status = new AccountTransactionStatusFilter();
  private AccountTransactionTypeFilter type = new AccountTransactionTypeFilter();
  private DateFilter createdDate = new DateFilter();

  @Data
  public static class AccountTransactionStatusFilter {

    private AccountTransactionStatus equals;
    private List<AccountTransactionStatus> in;
  }

  @Data
  public static class AccountTransactionTypeFilter {

    private AccountTransactionType equals;
    private List<AccountTransactionType> in;
  }
}
