package uk.gov.laa.ccms.springboot.auth;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * The API {@link Authentication} token representing a successfully authenticated client.
 */
public class ApiAuthenticationToken extends AbstractAuthenticationToken {

    private final String clientName;
    private final String accessToken;

    public ApiAuthenticationToken(String clientName, String accessToken,
                                  Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.clientName = clientName;
        this.accessToken = accessToken;
        this.setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return accessToken;
    }

    @Override
    public Object getPrincipal() {
        return clientName;
    }

}
