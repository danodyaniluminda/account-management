package com.biapay.accountmanagement.config;

import jakarta.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EntityManagerConfig {

  private final Provider<EntityManager> entityManagerProvider;

  @Inject
  public EntityManagerConfig(Provider<EntityManager> entityManagerProvider) {
    this.entityManagerProvider = entityManagerProvider;
  }

  @Bean
  public CriteriaBuilder criteriaBuilder(EntityManager entityManager) {
    return entityManager.getCriteriaBuilder();
  }

  @Bean
  public EntityManager entityManager() {
    return entityManagerProvider.get();
  }
}
