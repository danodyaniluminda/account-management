package com.biapay.accountmanagement.dto;

import com.biapay.core.model.UserType;
import com.biapay.core.model.enums.AccountType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InternalAccountBalanceReq {

  private UserType userType;
  private Long userId;
  private AccountType accountType;
}
