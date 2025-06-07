package com.biapay.accountmanagement.service;

import com.biapay.accountmanagement.dto.AccountCreateReq;
import com.biapay.accountmanagement.dto.HoldingTransactionDto;
import com.biapay.core.constant.MessageType;
import com.biapay.core.dto.StreamMessageDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class KafkaConsumer {

  private final AccountService accountService;
  private final AccountTransactionService accountTransactionService;
  private final AccountTransactionServiceV2 accountTransactionServiceV2;
  private final ObjectMapper objectMapper;

  @KafkaListener(
      topics = "${spring.kafka.topics.account-management}",
      groupId = "${spring.kafka.consumer.group-id}")
  public void consume(String message) {
    try {
      Gson gson = new Gson();

      StreamMessageDTO streamMessageDTO = gson.fromJson(message, StreamMessageDTO.class);
      log.info("Received message for processing: {}", streamMessageDTO.getType());
      if (streamMessageDTO.getType().equals(MessageType.ACCOUNT_CREATION_REQUEST)) {
        AccountCreateReq accountCreateReq = gson.fromJson(streamMessageDTO.getPayload().toString(), AccountCreateReq.class);
        accountService.createAccountByUser(accountCreateReq);

        log.info("Processed message for processing: {}", streamMessageDTO.getType());
      }

      if(streamMessageDTO.getType().equals(MessageType.ACCOUNT_TRANSACTION_REQUEST)) {
        if (streamMessageDTO.getPayload() != null) {
          Map<String, Object> payloadMap = (Map<String, Object>) streamMessageDTO.getPayload();
          accountTransactionServiceV2.initTransaction(UUID.fromString((String) payloadMap.get("clientTransactionId")));
        }
      }

      if(streamMessageDTO.getType().equals(MessageType.SETTLE_HOLD_AMOUNT_REQUEST)) {
        if (streamMessageDTO.getPayload() != null) {
          JsonElement jsonElement = gson.toJsonTree(streamMessageDTO.getPayload());
          HoldingTransactionDto holdingTransactionDto = gson.fromJson(jsonElement, HoldingTransactionDto.class);
          accountTransactionServiceV2.syncHoldingAmountAfterSettlement(holdingTransactionDto);

          log.info("Processed message for processing: {}", streamMessageDTO.getType());
        }
      }

      if (streamMessageDTO.getType().equals(MessageType.ACCOUNT_SETTLEMENT_TRANSACTION_REQUEST)) {
        if (streamMessageDTO.getPayload() != null) {
          Map<String, Object> payloadMap = (Map<String, Object>) streamMessageDTO.getPayload();
          accountTransactionService.initTransaction((String) payloadMap.get("clientTransactionId"));
        }

        log.info("Processed message for processing: {}", streamMessageDTO.getType());
      }
    } catch (Exception e) {
      log.error("Error while processing message", e);
    }
  }
}
