package com.biapay.accountmanagement.config;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * Configuration of web application with Servlet 3.0 APIs.
 */
@Configuration
public class WebConfigurer implements ServletContextInitializer {

  private final Logger log = LoggerFactory.getLogger(WebConfigurer.class);

  private final Environment env;

  public WebConfigurer(Environment env) {
    this.env = env;
  }

  @Override
  public void onStartup(ServletContext servletContext) throws ServletException {
    if (env.getActiveProfiles().length != 0) {
      log.info(
          "Web application configuration, using profiles: {}",
          (Object[]) env.getActiveProfiles());
    }

    log.info("Web application fully configured");
  }
}
