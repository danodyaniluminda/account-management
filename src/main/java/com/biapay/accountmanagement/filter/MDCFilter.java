package com.biapay.accountmanagement.filter;

import java.io.IOException;
import java.util.UUID;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class MDCFilter extends OncePerRequestFilter {
  public static final String REQUEST_ID = "REQUEST_ID";

  @Override
  protected void doFilterInternal(
      HttpServletRequest httpServletRequest,
      HttpServletResponse httpServletResponse,
      FilterChain filterChain)
      throws ServletException, IOException {
    try {
      String requestId = UUID.randomUUID().toString();
      MDC.put(REQUEST_ID, requestId);
      httpServletResponse.setHeader("x-" + REQUEST_ID, requestId);
      filterChain.doFilter(httpServletRequest, httpServletResponse);
    } finally {
      MDC.clear();
    }
  }
}
