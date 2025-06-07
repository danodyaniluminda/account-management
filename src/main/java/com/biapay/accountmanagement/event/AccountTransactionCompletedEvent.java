package com.biapay.accountmanagement.event;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class AccountTransactionCompletedEvent {

  private Long accountTransactionId;
}
