package uk.gov.laa.springboot.oauth2;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * OAuth2 authorization properties used by the starter.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "laa.springboot.starter.oauth2")
public class Oauth2AuthorizationProperties {

  public static final String DEFAULT_ROLES_CLAIM_PATH = "roles";
  public static final String DEFAULT_SCOPES_CLAIM_PATH = "scope";
  public static final String DEFAULT_ROLE_AUTHORITY_PREFIX = "ROLE_";
  public static final String DEFAULT_SCOPE_AUTHORITY_PREFIX = "SCOPE_";

  private String authorizedRoles = "[]";
  private String authorizedScopes = "[]";
  private String[] unprotectedUris = new String[0];
  private String rolesClaimPath = DEFAULT_ROLES_CLAIM_PATH;
  private String scopesClaimPath = DEFAULT_SCOPES_CLAIM_PATH;
  private String roleAuthorityPrefix = DEFAULT_ROLE_AUTHORITY_PREFIX;
  private String scopeAuthorityPrefix = DEFAULT_SCOPE_AUTHORITY_PREFIX;
  private Resourceserver resourceserver = new Resourceserver();

  /** OAuth2 resource-server properties managed by the starter. */
  @Getter
  @Setter
  public static class Resourceserver {
    private MultiTenantJwtProperties jwt = new MultiTenantJwtProperties();
  }
}
