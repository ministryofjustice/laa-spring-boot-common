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

  private String authorizedRoles = "[]";
  private String authorizedScopes = "[]";
  private String[] unprotectedUris = new String[0];
  private String rolesClaimPath = "roles";
  private String scopesClaimPath = "scope";
  private String roleAuthorityPrefix = "ROLE_";
  private String scopeAuthorityPrefix = "SCOPE_";
}
