package com.biapay.accountmanagement.dto;

import com.biapay.core.model.UserType;
import com.biapay.core.model.enums.AccountType;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class InternalAccountHistoryReq {

  private List<String> currencyCodes;

  private LocalDate fromDate;

  private LocalDate toDate;

  private int page;

  private int size;

  private UserType userType;

  private String userId;

  private AccountType accountType;
}
