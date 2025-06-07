package com.biapay.accountmanagement.dto;

import com.biapay.core.model.enums.AccountSubType;
import java.io.Serializable;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class SettlementAccountCreateReq implements Serializable {
  private Long accountUserId;
  private String accountNumber;
  private AccountSubType accountSubType;
  private Long settlementMethodId;
  private Long paymentMethodId;
}
