//package com.biapay.accountmanagement.filter;
//
//import com.biapay.accountmanagement.util.JwtTokenUtil;
//import java.io.IOException;
//import java.util.List;
//import javax.servlet.FilterChain;
//import javax.servlet.ServletException;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//@Component("jwtRequestFilter")
//@Slf4j
//public class JwtRequestFilter extends OncePerRequestFilter {
//  @Autowired private JwtTokenUtil jwtTokenUtil;
//
//  /**
//   * This filter will apply if Authorization is found in header or in query parameter
//   *
//   * @param request
//   * @param response
//   * @param chain
//   * @throws ServletException
//   * @throws IOException
//   */
//  @Override
//  protected void doFilterInternal(
//      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
//      throws ServletException, IOException {
//    String token = null;
//    if (request.getHeader("Authorization") != null
//        && request.getHeader("Authorization").startsWith("Bearer ")) {
//      token = request.getHeader("Authorization").substring(7);
//    }
//    if (request.getParameter("Authorization") != null) {
//      token = request.getParameter("Authorization");
//    }
//    try {
//      if (token != null && jwtTokenUtil.validateToken(token)) {
//        try {
//          Integer id =
//              jwtTokenUtil.getClaimFromToken(token, claims -> claims.get("id", Integer.class));
//          String lang =
//              jwtTokenUtil.getClaimFromToken(token, claims -> claims.get("lang", String.class));
//          String subject =
//              jwtTokenUtil.getClaimFromToken(token, claims -> claims.get("sub", String.class));
//          String userType =
//              jwtTokenUtil.getClaimFromToken(token, claims -> claims.get("userType", String.class));
//          String merchant =
//              jwtTokenUtil.getClaimFromToken(token, claims -> claims.get("merchant", String.class));
//          List<String> roles =
//              jwtTokenUtil.getClaimFromToken(token, claims -> claims.get("roles", List.class));
//          List<String> permissions =
//              jwtTokenUtil.getClaimFromToken(
//                  token, claims -> claims.get("permissions", List.class));
//
//          //  GatewayAuthenticationToken gatewayAuthenticationToken =
//          //                            new GatewayAuthenticationToken(
//          //                                    subject,
//          //                                    null,
//          //                                    id,
//          //                                    lang,
//          //                                    userType,
//          //                                    merchant,
//          //                                    roles,
//          //                                    permissions,
//          //                                    clientDetails,
//          //                                    clientUserDetails,
//          //                                    orderDetails,
//          //                                    transactionDetails);
//          //                    gatewayAuthenticationToken.setDetails(
//          //                            new WebAuthenticationDetailsSource().buildDetails(request));
//          //
//          // SecurityContextHolder.getContext().setAuthentication(gatewayAuthenticationToken);
//
//        } catch (Exception e) {
//          log.warn(e.getMessage(), e);
//        }
//      }
//    } catch (Exception e) {
//      log.warn(e.getMessage(), e);
//    }
//    chain.doFilter(request, response);
//  }
//}
