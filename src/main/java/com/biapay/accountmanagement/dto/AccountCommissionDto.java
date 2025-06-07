package com.biapay.accountmanagement.dto;

import com.biapay.core.constant.enums.AccountTransactionStatus;
import com.biapay.core.model.UserType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class AccountCommissionDto {

  private Instant transactionDate;
  private String customerName;
  private Long customerAccountId;
  private UserType customerType;

  private String agentName;
  private Long agentAccountId;

  @Enumerated(EnumType.STRING)
  private UserType agentType;

  private String currencyCode;
  private String operation;

  private BigDecimal amount = BigDecimal.ZERO;
  private BigDecimal fee = BigDecimal.ZERO;
  private BigDecimal commission = BigDecimal.ZERO;

  private String subscriptionPlanId;
  private String commissionType;
  private double commissionPercentage;
  private BigDecimal commissionAmount = BigDecimal.ZERO;

  private String supervisorAccountId;

  private String receiverAccount;
  private UserType receiverType;

  @Enumerated(EnumType.STRING)
  private AccountTransactionStatus status;

  private int isSettled = 0;
  private Date settlementDate;
  private Date settledAt;
}
