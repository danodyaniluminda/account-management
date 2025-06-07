package com.biapay.accountmanagement.scheduler;

import com.biapay.accountmanagement.dto.AccountCreateReq;
import com.biapay.accountmanagement.service.AccountService;
import com.biapay.core.model.User;
import com.biapay.core.model.UserType;
import com.biapay.core.repository.AccountRepository;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AccountCreationScheduler {

  @Autowired
  private AccountRepository accountRepository;

  @Autowired
  private AccountService accountService;

  @Scheduled(cron = "0 */7 * ? * *")
  public void createAccounts(){
//    first get pending users
    log.info("Check if any pending users are there to create account");
    List<User> pendingUsers = accountRepository.getPendingUsersForAccountCreation();
    if (!pendingUsers.isEmpty()){
      log.info("Found {} users to create accounts!", pendingUsers.size());

      try{
        for (User pendingUser : pendingUsers){
          if (pendingUser.getUserType().equals(UserType.USER)){
            log.info("Processing account creation for {}", pendingUser.getMobileNumber());
            accountService.createAccountByUser(AccountCreateReq.builder().accountUserId(pendingUser.getUserId()).build());
          }
        }
      } catch (Exception ex){
        log.error("Failed to create account for error: {}", ex.getMessage());
      }

    }
  }
}
