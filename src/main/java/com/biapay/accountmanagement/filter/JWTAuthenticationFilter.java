package com.biapay.accountmanagement.filter;

import com.biapay.accountmanagement.exception.BIARuntimeException;
import com.biapay.core.constant.BIAConstants;
import com.biapay.core.dto.LoginRequestDTO;
import com.biapay.core.repository.AccessHistoryRepository;
import com.biapay.core.repository.AdminRepository;
import com.biapay.core.repository.UserRepository;
import com.biapay.core.security.CustomAuthenticationFailureHandler;
import com.biapay.core.util.JsonUtil;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Slf4j
public class JWTAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
  private AuthenticationManager authenticationManager;
  private String jwtSecret;
  private Long jwtExpirationTime;
  private Integer maxLoginAttemps;
  private UserRepository userRepository;
  private AccessHistoryRepository accessHistoryRepository;
  private AdminRepository adminRepository;

  public JWTAuthenticationFilter(
      AuthenticationManager authenticationManager, UserRepository userRepository) {
    this.authenticationManager = authenticationManager;
    this.userRepository = userRepository;

    this.setAuthenticationFailureHandler(new CustomAuthenticationFailureHandler());
    setFilterProcessesUrl(BIAConstants.Auth.LOGIN_URL);
  }

  @Override
  public Authentication attemptAuthentication(
      HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
    LoginRequestDTO loginRequestDTO = null;
    try {
      loginRequestDTO = JsonUtil.toObject(request.getInputStream(), LoginRequestDTO.class);
      log.info(
          "user: {} with type: {} is trying to login",
          loginRequestDTO.getEmail(),
          loginRequestDTO.getUserType());
      return authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(
              JsonUtil.serialize(loginRequestDTO), loginRequestDTO.getPassword()));
    } catch (BadCredentialsException e) {
      log.warn(e.getMessage());
      //            increaseFailedLoggedInCountAndLockIfRequired(loginRequestDTO);
      throw e;
    } catch (AuthenticationException e) {
      log.warn(e.getMessage(), e);
      throw e;
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      throw new BIARuntimeException(e);
    }
  }

  //    @Override
  //    protected void successfulAuthentication(
  //            HttpServletRequest request,
  //            HttpServletResponse response,
  //            FilterChain chain,
  //            Authentication authResult)
  //            throws IOException {
  //        User user = (User) authResult.getPrincipal();
  //        resetLoginAttemptCount(user);
  //        if (user.getUserType() == UserType.ADMIN) {
  //            AdminUser adminUser = adminRepository.findByEmail(user.getEmail());
  //            if (adminUser != null && adminUser.getAdminGroup() != null) {
  //                adminUser.getAdminGroup().stream()
  //                        .map(adminGroup -> adminGroup.getRoles())
  //                        .forEach(roles -> user.getRoles().addAll(roles));
  //            }
  //        }
  //        String token = JWTTokenUtil.generateToken(user, jwtSecret, jwtExpirationTime);
  //        response.getWriter().write(token);
  //        response.getWriter().flush();
  //        this.activehistory(request, user);
  //    }
  //
  //    public AccessHistory activehistory(HttpServletRequest request, User user) {
  //
  //        AccessHistory previousAccess =
  //                accessHistoryRepository.findFirstByUserOrderByLastModifiedDateDesc(user);
  //
  //        AccessHistory accesshistory = new AccessHistory();
  //        if (request.getHeader("X-Forward-For") != null) {
  //            accesshistory.setIpAddress(request.getHeader("X-Forward-For"));
  //        } else {
  //            accesshistory.setIpAddress(request.getRemoteAddr());
  //        }
  //        accesshistory.setBrowser(request.getHeader("User-Agent"));
  //        if (previousAccess != null
  //                && !previousAccess.getBrowser().equals(accesshistory.getBrowser())
  //                && !previousAccess.getIpAddress().equals(accesshistory.getIpAddress())) {
  //            notificationService.newBrowserDetectedEmail(
  //                    user, request.getHeader("User-Agent"), accesshistory.getIpAddress());
  //        }
  //        accesshistory.setUser(user);
  //        accesshistory.setDate(new Date(System.currentTimeMillis()));
  //        accessHistoryRepository.save(accesshistory);
  //        return accesshistory;
  //    }

  //    private void increaseFailedLoggedInCountAndLockIfRequired(LoginRequestDTO loginRequestDTO) {
  //        Optional<User> user =
  //                userRepository.findUserByEmailAndUserType(
  //                        loginRequestDTO.getEmail(), loginRequestDTO.getUserType());
  //        if (user.isPresent()) {
  //            user.get().setFailedLoginCount(user.get().getFailedLoginCount() + 1);
  //            if (user.get().getFailedLoginCount() > maxLoginAttemps) {
  //                log.info(
  //                        "Locking user: {} and type: {}",
  //                        loginRequestDTO.getEmail(),
  //                        loginRequestDTO.getUserType());
  //                user.get().setUserStatus(UserStatus.LOCKED);
  //            }
  //            userRepository.save(user.get());
  //        }
  //    }
  //
  //    private void resetLoginAttemptCount(User user) {
  //        user.setFailedLoginCount(0);
  //        userRepository.save(user);
  //    }
}
