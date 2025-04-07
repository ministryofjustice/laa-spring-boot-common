package uk.gov.laa.ccms.springboot.auth;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * API authentication service responsible for determining the authentication outcome for a given
 * client request.
 */
@Slf4j
public class ApiAuthenticationProvider implements AuthenticationProvider {

  private final TokenDetailsManager apiAuthenticationContextHolder;

  protected ApiAuthenticationProvider(TokenDetailsManager tokenDetailsManager) {
    this.apiAuthenticationContextHolder = tokenDetailsManager;
  }

  /**
   * Authenticate the HTTP request, comparing the access token provided by the client against the
   *     list of authorized client details configured in the application properties.
   *
   * @param authentication the HTTP request made to the API
   * @return {@link Authentication} outcome with the list of roles assumed by the client
   *     if successful.
   * @throws BadCredentialsException when authentication fails
   */
  @Override
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {

    String accessToken = Optional.ofNullable(authentication.getCredentials()).map(Object::toString)
        .orElseThrow(() -> new MissingCredentialsException("No API access token provided."));

    if (!apiAuthenticationContextHolder.clientExistsForToken(accessToken)) {
      throw new BadCredentialsException("Invalid API access token provided.");
    }

    List<GrantedAuthority> grantedAuthorities =
        apiAuthenticationContextHolder.getClientRoles(accessToken).stream()
            .map(role -> "ROLE_" + role).map(SimpleGrantedAuthority::new)
            .collect(Collectors.toUnmodifiableList());

    if (grantedAuthorities.isEmpty()) {
      grantedAuthorities = AuthorityUtils.NO_AUTHORITIES;
    }

    return ApiAuthenticationToken.authenticated(
        apiAuthenticationContextHolder.getPrincipal(accessToken),
        accessToken, grantedAuthorities);
  }

  @Override
  public boolean supports(Class<?> authentication) {
    return authentication.equals(ApiAuthenticationToken.class);
  }
}
