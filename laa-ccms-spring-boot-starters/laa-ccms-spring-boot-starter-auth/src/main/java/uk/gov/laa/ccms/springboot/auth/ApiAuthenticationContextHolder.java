package uk.gov.laa.ccms.springboot.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.InvalidPropertyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Holds the authentication context for the API.
 */
@Slf4j
@Component
public class ApiAuthenticationContextHolder {

  private final AuthenticationProperties authenticationProperties;

  @Getter
  private String authenticationHeader;

  @Getter
  private String[] unprotectedUris;

  @Getter
  private Set<ClientCredential> clientCredentials;

  @Getter
  private Set<AuthorizedRole> authorizedRoles;

  @Autowired
  public ApiAuthenticationContextHolder(AuthenticationProperties authenticationProperties) {
    this.authenticationProperties = authenticationProperties;
  }

  /**
   * Load authentication context, including authorized clients and roles from JSON.
   */
  @PostConstruct
  private void initialize() {
    authenticationHeader = authenticationProperties.getAuthenticationHeader();
    unprotectedUris = authenticationProperties.getUnprotectedUris();
    initializeAuthorizedClients();
    initializeAuthorizedRoles();
  }

  /**
   * Initialise a set of {@link ClientCredential} from those configured as a JSON string in the
   * application properties.
   */
  private void initializeAuthorizedClients() {
    try {
      clientCredentials =
          new ObjectMapper()
              .readValue(
                  authenticationProperties.getAuthorizedClients(),
                  new TypeReference<Set<ClientCredential>>() {});
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }

    if (clientCredentials.isEmpty()) {
      throw new InvalidPropertyException(
          AuthenticationProperties.class,
          "authorizedClients",
          "At least one authorized client must be provided.");
    }

    for (ClientCredential clientCredential : clientCredentials) {
      log.info(
          "Authorized Client Registered: '{}' Roles: {}",
          clientCredential.name(),
          clientCredential.roles().toString());
    }
  }

  /**
   * Initialise a set of {@link AuthorizedRole} from those configured as a JSON string in the
   * application properties.
   */
  private void initializeAuthorizedRoles() {

    try {
      authorizedRoles =
          new ObjectMapper()
              .readValue(
                  authenticationProperties.getAuthorizedRoles(),
                  new TypeReference<Set<AuthorizedRole>>() {});
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }

    if (authorizedRoles.isEmpty()) {
      throw new InvalidPropertyException(
          AuthenticationProperties.class,
          "authorizedRoles",
          "At least one authorized role must be provided.");
    }

    for (AuthorizedRole authorizedRole : authorizedRoles) {
      log.info("Authorized Role Registered: '{}'", authorizedRole.name());
    }
  }

  /**
   * Retrieve the client details based on the access token provided.
   *
   * @param accessToken the client-provided access token
   * @return the {@link ClientCredential} associated with the access token
   */
  public Optional<ClientCredential> getMatchingClientCredential(String accessToken) {
    return clientCredentials.stream()
        .filter(credential -> credential.token().equals(accessToken))
        .findFirst();
  }

  /**
   * Retrieve a list of roles associated with the client, based on the access token provided. If the
   * client is not in the authorized list, no roles are returned.
   *
   * @param accessToken the client-provided access token
   * @return the set of roles associated with the access token, if authorized
   */
  public Set<String> getClientRoles(String accessToken) {
    return getMatchingClientCredential(accessToken)
        .map(ClientCredential::roles)
        .orElse(Collections.emptySet());
  }

  /**
   * Determine whether there is a client associated with the provided token.
   *
   * @param accessToken the client-provided access token
   * @return {@code true} if the access token is authorized and {@code false} otherwise.
   */
  public boolean clientExistsForToken(String accessToken) {
    return getMatchingClientCredential(accessToken).isPresent();
  }

  /**
   * Retrieve the principal (client name) based on the access token provided.
   *
   * @param accessToken the client-provided access token
   * @return the principal (client name) associated with the access token
   */
  public String getPrincipal(String accessToken) {
    return getMatchingClientCredential(accessToken).map(ClientCredential::name).orElse(null);
  }
}
