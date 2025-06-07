package com.biapay.accountmanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAspectJAutoProxy
@SpringBootApplication(scanBasePackages = {"com.biapay.core", "com.biapay.accountmanagement"})
@EnableJpaRepositories(basePackages = {"com.biapay.core", "com.biapay.accountmanagement"})
@EntityScan(basePackages = {"com.biapay.core", "com.biapay.accountmanagement"})
@EnableScheduling
public class AccountManagementApplication {

  public static void main(String[] args) {
    SpringApplication.run(AccountManagementApplication.class, args);
  }

}
