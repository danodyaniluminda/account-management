package com.biapay.accountmanagement.controller;

import com.biapay.accountmanagement.dto.AccountCreateReq;
import com.biapay.accountmanagement.dto.HoldingTransactionDto;
import com.biapay.accountmanagement.service.AccountService;
import com.biapay.accountmanagement.service.AccountTransactionService;
import com.biapay.accountmanagement.service.AccountTransactionServiceV2;
import com.biapay.accountmanagement.util.ResponseUtil;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class AccountController {

  @Autowired
  private AccountService accountService;

  @Autowired
  private AccountTransactionService accountTransactionService;

  @Autowired
  private AccountTransactionServiceV2 accountTransactionServiceV2;

//  @GetMapping(path = "/getAll", produces = MediaType.APPLICATION_JSON_VALUE)
//  public ResponseEntity<?> getAllAccounts(@RequestParam HashMap<String, String> allRequestParams) {
//    return ResponseUtil.response(accountService.getAccounts(allRequestParams));
//  }
//
//  @GetMapping(path = "/getAccountInfo/{accountNumber}", produces = MediaType.APPLICATION_JSON_VALUE)
//  public ResponseEntity<?> getAccountInfo(@PathVariable String accountNumber) {
//    return ResponseUtil.response(accountService.getAccountByNumber(accountNumber));
//  }

  @PostMapping(path = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> createAccount(@RequestBody AccountCreateReq accountCreateReq) {
    accountService.createAccountByUser(accountCreateReq);

    return ResponseEntity.ok().build();
  }

  /*@PostMapping(path = "/doTransaction", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> doTransaction(
      @RequestBody AccountTransactionRequest accountTransactionReq) {
//    accountTransactionService.initiateTransaction(accountTransactionReq);

    return ResponseEntity.ok().build();
  }

  @PostMapping(path = "/initPendingTransaction", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> initPendingTransaction(
      @RequestParam(name = "transactionRef") String transcationRef) {
//    accountTransactionService.initiateTransaction(accountTransactionReq);
    accountTransactionService.initTransaction(transcationRef);

    return ResponseEntity.ok().build();
  }

  @GetMapping(path = "/history", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> history(
      @RequestParam(value = "userId", required = false) Long userId,
      @RequestParam(value = "fromDate", required = true) String fromDate,
      @RequestParam(value = "toDate", required = true) String toDate,
      @RequestParam(value = "currency", required = true) String currency,
      @RequestParam(value = "page", required = false, defaultValue = "0") int page,
      @RequestParam(value = "size", required = false, defaultValue = "20") int size
      ) {
    return ResponseEntity.ok(accountService.accountHistory(userId, fromDate, toDate, currency, page, size));
  }*/

  @PostMapping(path = "/trigger/synHoldingAmount", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> settleHolingAmount(@RequestParam("clientTransactionId") String clientTransactionId) {
    HoldingTransactionDto holdingTransactionDto = new HoldingTransactionDto();
    holdingTransactionDto.setClientTransactionIds(Arrays.asList(clientTransactionId));
    accountTransactionServiceV2.syncHoldingAmountAfterSettlement(holdingTransactionDto);
    return ResponseEntity.ok().build();
  }

  @PostMapping(path = "/trigger/settlementTransaction", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> settleTransaction(@RequestParam("clientTransactionId") String clientTransactionId) {
    accountTransactionServiceV2.initTransaction(UUID.fromString(clientTransactionId));
    return ResponseEntity.ok().build();
  }

}
