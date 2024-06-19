package uk.gov.laa.ccms.springboot.auth;

import java.util.Collection;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.Assert;

/** The API {@link Authentication} token representing a successfully authenticated client. */
public class ApiAuthenticationToken extends AbstractAuthenticationToken {

  private final String clientName;
  private final String accessToken;

  /**
   * Constructs an {@code ApiAuthenticationToken} with client details,
   * and marks them as authenticated. This should only be used once the authentication handler
   * has confirmed the client's credentials.
   *
   * @param clientName the name of the client making the request.
   * @param accessToken the access token provided by the client.
   * @param authorities the roles associated with the client.
   */
  public ApiAuthenticationToken(
      String clientName, String accessToken, Collection<? extends GrantedAuthority> authorities) {
    super(authorities);
    this.clientName = clientName;
    this.accessToken = accessToken;
    super.setAuthenticated(true);
  }

  @Override
  public Object getCredentials() {
    return accessToken;
  }

  @Override
  public Object getPrincipal() {
    return clientName;
  }

  @Override
  public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
    Assert.isTrue(
        !isAuthenticated,
        "Cannot set this token to trusted - "
            + "use constructor which takes a GrantedAuthority list instead");
    super.setAuthenticated(false);
  }
}
