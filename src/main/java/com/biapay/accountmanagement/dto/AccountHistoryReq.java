package com.biapay.accountmanagement.dto;

import com.biapay.core.model.enums.AccountType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDate;
import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class AccountHistoryReq {

  @NotNull
  private List<String> currencyCodes;

  @NotNull
  private LocalDate fromDate;

  @NotNull
  private LocalDate toDate;

  private int page;

  private int size;

  @JsonIgnore
  private LoggedInUser loggedInUser;

  private AccountType accountType;
}
