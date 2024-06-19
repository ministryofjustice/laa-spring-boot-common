package uk.gov.laa.ccms.springboot.auth;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

/**
 * API authentication service responsible for determining the authentication outcome for a given
 * client request.
 */
@Slf4j
@Component
@EnableConfigurationProperties(AuthenticationProperties.class)
public class ApiAuthenticationService {

  private final ApiAuthenticationContextHolder apiAuthenticationContextHolder;

  @Autowired
  protected ApiAuthenticationService(
      ApiAuthenticationContextHolder apiAuthenticationContextHolder) {
    this.apiAuthenticationContextHolder = apiAuthenticationContextHolder;
  }

  /**
   * Authenticate the HTTP request, comparing the access token provided by the client against the
   * list of authorized client details configured in the application properties.
   *
   * @param request the HTTP request made to the API
   * @return {@link Authentication} outcome with the list of roles assumed by the client if
   *     successful
   * @throws BadCredentialsException when authentication fails
   */
  protected Authentication getAuthentication(HttpServletRequest request) {
    String accessToken =
        request.getHeader(apiAuthenticationContextHolder.getAuthenticationHeader());

    if (accessToken == null) {
      throw new MissingCredentialsException("No API access token provided.");
    }

    if (!apiAuthenticationContextHolder.clientExistsForToken(accessToken)) {
      throw new BadCredentialsException("Invalid API access token provided.");
    }

    List<GrantedAuthority> grantedAuthorities =
        apiAuthenticationContextHolder.getClientRoles(accessToken).stream()
            .map(role -> "ROLE_" + role)
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toUnmodifiableList());

    if (grantedAuthorities.isEmpty()) {
      grantedAuthorities = AuthorityUtils.NO_AUTHORITIES;
    }

    return new ApiAuthenticationToken(
        apiAuthenticationContextHolder.getPrincipal(accessToken), accessToken, grantedAuthorities);
  }
}
