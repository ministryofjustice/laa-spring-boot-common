package uk.gov.laa.ccms.springboot.auth;

import java.util.Collection;
import java.util.Collections;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.Assert;

/**
 * The API {@link Authentication} token representing a successfully authenticated client.
 */
public class ApiAuthenticationToken extends AbstractAuthenticationToken {

  private String clientName;
  private final String accessToken;

  /**
   * Constructs an {@code ApiAuthenticationToken} with client details for authentication.
   *
   * @param accessToken the access token provided by the client.
   */
  private ApiAuthenticationToken(String accessToken) {
    super(Collections.emptyList());
    this.accessToken = accessToken;
    super.setAuthenticated(false);
  }

  /**
   * Constructs an {@code ApiAuthenticationToken} with client details,
   * and marks them as authenticated. This should only be used once the authentication handler
   * has confirmed the client's credentials.
   *
   * @param clientName  the name of the client making the request.
   * @param accessToken the access token provided by the client.
   * @param authorities the roles associated with the client.
   */
  private ApiAuthenticationToken(
      String clientName, String accessToken, Collection<? extends GrantedAuthority> authorities) {
    super(authorities);
    this.clientName = clientName;
    this.accessToken = accessToken;
    super.setAuthenticated(true);
  }


  public static ApiAuthenticationToken authenticated(
      String clientName, String accessToken, Collection<? extends GrantedAuthority> authorities) {
    return new ApiAuthenticationToken(clientName, accessToken, authorities);
  }

  public static ApiAuthenticationToken unauthenticated(String accessToken) {
    return new ApiAuthenticationToken(accessToken);
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

  @Override
  public boolean equals(Object o) {
    return super.equals(o);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }
}
