package uk.gov.laa.ccms.springboot.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.InvalidPropertyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * API authentication service responsible for determining the authentication outcome for a given client request.
 */
@Slf4j
@Component
@EnableConfigurationProperties(AuthenticationProperties.class)
public class ApiAuthenticationService {

    private final AuthenticationProperties authenticationProperties;

    private Set<ClientCredential> clientCredentials;

    @Autowired
    protected ApiAuthenticationService(AuthenticationProperties authenticationProperties) {
        this.authenticationProperties = authenticationProperties;
    }

    /**
     * Initialise a set of {@link ClientCredential} from those configured as a JSON string in the application
     * properties.
     */
    @PostConstruct
    private void initialise() {
        try {
            clientCredentials = new ObjectMapper().readValue(authenticationProperties.getAuthorizedClients()
                    , new TypeReference<Set<ClientCredential>>(){});
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        if (clientCredentials.isEmpty()) throw new InvalidPropertyException(AuthenticationProperties.class,
                "authorizedClients", "At least one authorized client must be provided.");

        for (ClientCredential clientCredential : clientCredentials) {
            log.info("Authorized Client Registered: '{}' Roles: {}", clientCredential.name(), clientCredential.roles().toString());
        }
    }

    /**
     * Authenticate the HTTP request, comparing the access token provided by the client against the list of authorized
     * client details configured in the application properties.
     *
     * @param request the HTTP request made to the API
     * @return {@link Authentication} outcome with the list of roles assumed by the client if successful
     * @throws BadCredentialsException when authentication fails
     */
    protected Authentication getAuthentication(HttpServletRequest request) {
        String accessToken = request.getHeader(authenticationProperties.getAuthenticationHeader());

        if (accessToken == null) {
            throw new MissingCredentialsException("No API access token provided.");
        }

        if (!isAuthorizedAccessToken(accessToken)) {
            throw new BadCredentialsException("Invalid API access token provided.");
        }

        List<GrantedAuthority> grantedAuthorities = getClientRoles(accessToken)
                .stream()
                .map(role -> "ROLE_" + role)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toUnmodifiableList());

        if (grantedAuthorities.isEmpty()) grantedAuthorities = AuthorityUtils.NO_AUTHORITIES;

        return new ApiAuthenticationToken(getPrincipal(accessToken), accessToken, grantedAuthorities);
    }

    /**
     * Retrieve a list of roles associated with the client, based on the access token provided. If the client is
     * not in the authorized list, no roles are returned.
     *
     * @param accessToken the client-provided access token
     * @return the list of roles associated with the access token, if authorized
     */
    private Set<String> getClientRoles(String accessToken) {
        return getMatchingClientCredential(accessToken)
                .map(ClientCredential::roles)
                .orElse(Collections.emptySet());
    }

    /**
     * Determine whether an access token is authorized.
     *
     * @param accessToken the client-provided access token
     * @return {@code true} if the access token is authorized and {@code false} otherwise.
     */
    private boolean isAuthorizedAccessToken(String accessToken) {
        return getMatchingClientCredential(accessToken)
                .isPresent();
    }

    /**
     * Retrieve the principal (client name) based on the access token provided.
     *
     * @param accessToken the client-provided access token
     * @return the principal (client name) associated with the access token
     */
    private String getPrincipal(String accessToken) {
        return getMatchingClientCredential(accessToken)
                .map(ClientCredential::name)
                .orElse(null);
    }

    /**
     * Retrieve the client details based on the access token provided.
     *
     * @param accessToken the client-provided access token
     * @return the {@link ClientCredential} associated with the access token
     */
    private Optional<ClientCredential> getMatchingClientCredential(String accessToken) {
        return clientCredentials.stream()
                .filter(credential -> credential.token().equals(accessToken))
                .findFirst();
    }
}
