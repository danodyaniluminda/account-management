package com.biapay.accountmanagement.config;

import com.biapay.accountmanagement.filter.MDCFilter;
import com.biapay.core.exception.UnAuthorizedException;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.keycloak.adapters.KeycloakConfigResolver;
import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver;
import org.keycloak.adapters.springsecurity.KeycloakConfiguration;
import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationProvider;
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter;
import org.keycloak.adapters.springsecurity.filter.KeycloakAuthenticatedActionsFilter;
import org.keycloak.adapters.springsecurity.filter.KeycloakAuthenticationProcessingFilter;
import org.keycloak.adapters.springsecurity.filter.KeycloakPreAuthActionsFilter;
import org.keycloak.adapters.springsecurity.filter.KeycloakSecurityContextRequestFilter;
import org.keycloak.adapters.springsecurity.management.HttpSessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.preauth.x509.X509AuthenticationFilter;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.request.async.WebAsyncManagerIntegrationFilter;

@EnableGlobalMethodSecurity(jsr250Enabled = true, securedEnabled = true, prePostEnabled = true)
@KeycloakConfiguration
public class SecurityConfiguration extends KeycloakWebSecurityConfigurerAdapter {

  private static final String[] SWAGGER_WHITELIST = {
    "/v2/api-docs",
    "/configuration/**",
    "/swagger-resources/**",
    "/configuration/security",
    "/swagger-ui/**",
    "/webjars/**"
  };
  private static final String[] PUBLIC_URLS = {
    "/api/public/**",
    "/invoice/public/**",
    "/invoice/billing/public/**",
    "/invoice/billing/createBillingForDefaultPlan",
    "/invoice/product/getAllListedProducts"
  };
  @Autowired private MDCFilter mdcFilter;

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.cors()
        .and()
        .csrf()
        .disable()
        .sessionManagement()
        .sessionAuthenticationStrategy(sessionAuthenticationStrategy())
        .and()
        .addFilterBefore(keycloakPreAuthActionsFilter(), LogoutFilter.class)
        .addFilterBefore(keycloakAuthenticationProcessingFilter(), X509AuthenticationFilter.class)
//        .addFilterBefore(mdcFilter, WebAsyncManagerIntegrationFilter.class)
        .exceptionHandling()
        .authenticationEntryPoint(authenticationEntryPoint())
        .and()
        .formLogin()
        .disable()
        .httpBasic()
        .disable()
        .logout()
        .disable()
        .sessionManagement()
        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        .and()
        .authorizeRequests()
        .antMatchers(HttpMethod.OPTIONS, "/**")
        .permitAll()
        .antMatchers(PUBLIC_URLS)
        .permitAll()
        .antMatchers(SWAGGER_WHITELIST)
        .permitAll()
        .anyRequest()
        .authenticated();
  }

  @Override
  public void configure(WebSecurity web) throws Exception {
    web.ignoring().antMatchers(SWAGGER_WHITELIST).antMatchers(PUBLIC_URLS);
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Override
  protected KeycloakAuthenticationProcessingFilter keycloakAuthenticationProcessingFilter()
      throws Exception {
    KeycloakAuthenticationProcessingFilter filter =
        new KeycloakAuthenticationProcessingFilter(authenticationManager());
    filter.setSessionAuthenticationStrategy(sessionAuthenticationStrategy());
    filter.setAuthenticationFailureHandler(
        (httpServletRequest, httpServletResponse, e) -> {
          httpServletResponse.setStatus(HttpStatus.SC_UNAUTHORIZED);
          httpServletResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
          httpServletResponse
              .getWriter()
              .write(new UnAuthorizedException("UnAuthorized Access").getDefaultBodyAsString());
          httpServletResponse.getWriter().flush();
        });
    return filter;
  }

  @Bean
  public AccessDeniedHandler accessDeniedHandler() {
    return new CustomAccessDeniedHandler();
  }

  @Bean
  public AuthenticationEntryPoint authenticationEntryPoint() {
    return new CustomAuthenticationEntryPoint();
  }

  @Bean
  public KeycloakConfigResolver KeycloakConfigResolver() {
    return new KeycloakSpringBootConfigResolver();
  }

  @Override
  protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
    /*
     Returning NullAuthenticatedSessionStrategy means app will not remember
     session
    */
    return new NullAuthenticatedSessionStrategy();
  }

  @Autowired
  public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
    KeycloakAuthenticationProvider keycloakAuthenticationProvider =
        keycloakAuthenticationProvider();

    keycloakAuthenticationProvider.setGrantedAuthoritiesMapper(new SimpleAuthorityMapper());

    auth.authenticationProvider(keycloakAuthenticationProvider);
  }

  @Bean
  public FilterRegistrationBean<?> keycloakAuthenticationProcessingFilterRegistrationBean(
      KeycloakAuthenticationProcessingFilter filter) {
    FilterRegistrationBean<?> registrationBean = new FilterRegistrationBean<>(filter);
    registrationBean.setEnabled(false);
    return registrationBean;
  }

  @Bean
  public FilterRegistrationBean<?> keycloakPreAuthActionsFilterRegistrationBean(
      KeycloakPreAuthActionsFilter filter) {

    FilterRegistrationBean<?> registrationBean = new FilterRegistrationBean<>(filter);
    registrationBean.setEnabled(false);
    return registrationBean;
  }

  @Bean
  public FilterRegistrationBean<?> keycloakAuthenticatedActionsFilterBean(
      KeycloakAuthenticatedActionsFilter filter) {

    FilterRegistrationBean<?> registrationBean = new FilterRegistrationBean<>(filter);

    registrationBean.setEnabled(false);
    return registrationBean;
  }

  @Bean
  public FilterRegistrationBean<?> keycloakSecurityContextRequestFilterBean(
      KeycloakSecurityContextRequestFilter filter) {

    FilterRegistrationBean<?> registrationBean = new FilterRegistrationBean<>(filter);

    registrationBean.setEnabled(false);

    return registrationBean;
  }

  @Bean
  @Override
  @ConditionalOnMissingBean(HttpSessionManager.class)
  protected HttpSessionManager httpSessionManager() {
    return new HttpSessionManager();
  }

  @Slf4j
  private static class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(
        HttpServletRequest request, HttpServletResponse response, AccessDeniedException e)
        throws IOException, ServletException, IOException {
      log.error("Access Denied: {}", e.getMessage());
      response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
    }
  }

  @Slf4j
  private static class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(
        HttpServletRequest request,
        HttpServletResponse response,
        AuthenticationException authException)
        throws IOException, ServletException {
      log.error("Unauthorized Request: {}", authException.getMessage());
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized Request");
    }
  }
}
