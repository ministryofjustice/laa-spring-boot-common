package uk.gov.laa.ccms.springboot.auth;

import java.util.Set;

/**
 * Holds reference authentication information for one client, populated from details configured in application
 * properties.
 *
 * @param name the name of the client
 * @param token the access token designated to the client
 * @param roles the roles designated to the client, which determine level of access
 */
public record ClientCredential(String name, String token, Set<String> roles) { }
