package com.biapay.accountmanagement.scheduler;

import com.biapay.accountmanagement.service.AccountTransactionServiceV2;
import com.biapay.core.model.enums.SettlementStatus;
import com.biapay.core.repository.SettlementRepository;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@AllArgsConstructor
@Component
public class SyncApprovedSettlementJob {
  private final SettlementRepository settlementRepository;
  private final AccountTransactionServiceV2 accountTransactionServiceV2;

  @Scheduled(cron = "0 0 * * * *")
  public void execute() {
    log.info("Executing sync job for approved settlement");

    Date twentyFourHoursAgo = Date.from(
        LocalDateTime.now().minusHours(24)
            .atZone(ZoneId.systemDefault())
            .toInstant()
    );
    List<UUID> approvedSettlementClientTransactionIds =  settlementRepository
        .findClientTransactionIdsByStatusAndCreatedDateAfter(SettlementStatus.APPROVED, twentyFourHoursAgo);

    // fetch all APPROVED settlement for last 24h
    approvedSettlementClientTransactionIds.stream()
            .forEach(clientTransactionId -> accountTransactionServiceV2.initTransaction(clientTransactionId));

    log.info("Finished sync approved settlement job");
  }
}
