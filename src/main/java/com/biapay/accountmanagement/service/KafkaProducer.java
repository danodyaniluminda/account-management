package com.biapay.accountmanagement.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class KafkaProducer {

  private final KafkaTemplate<String, String> kafkaTemplate;
  @Value("${spring.kafka.topics.notification}")
  private String notificationTopic;

  public KafkaProducer(KafkaTemplate<String, String> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
  }

//  public void sendNotification(NotificationMessageDTO notificationMessageDTO) {
//    try {
//      this.kafkaTemplate.send(notificationTopic, JsonUtil.toJsonString(notificationMessageDTO));
//    } catch (Exception e) {
//      log.error("Could not send message to notification service. {}", e.getMessage());
//    }
//    log.info("Sending notification: {} to notification service", "");
//  }
}
