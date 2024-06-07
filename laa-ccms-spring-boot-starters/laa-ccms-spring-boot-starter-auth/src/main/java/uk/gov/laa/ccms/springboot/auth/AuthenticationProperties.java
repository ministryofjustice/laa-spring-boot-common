package uk.gov.laa.ccms.springboot.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Authentication properties that must be configured by an API that is implementing authentication using this library.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "laa.ccms.springboot.starter.auth")
public class AuthenticationProperties {

    /**
     * The name of the HTTP header used to store the API access token.
     */
    @NotBlank(message = "authenticationHeader is required")
    private String authenticationHeader;

    /**
     * The list of clients who are authorized to access the API, and their roles
     * JSON formatted string, with the top level being a list and each contained item
     * representing a {@link ClientCredential}.
     */
    @NotBlank(message = "authorizedClients is required")
    private String authorizedClients;

    /**
     * The list of roles that can be used to access the API, and the URIs they enable access to.
     * JSON formatted string, with the top level being a list and each contained item representing
     * an {@link AuthorizedRole}.
     */
    @NotBlank(message = "authorizedRoles is required")
    private String authorizedRoles;

    /**
     * The list of URIs which do not require any authentication.
     */
    @NotBlank(message = "unprotectedURIs is required")
    private String[] unprotectedURIs;

}
