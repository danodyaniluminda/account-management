package com.biapay.accountmanagement.dto;

import com.biapay.core.model.UserType;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class AccounttUpdateReq {

  @NotNull
  private String phoneNumber;

  @NotNull
  private UserType accountUserType;

  @NotNull
  private String newPhoneNumber;
}
