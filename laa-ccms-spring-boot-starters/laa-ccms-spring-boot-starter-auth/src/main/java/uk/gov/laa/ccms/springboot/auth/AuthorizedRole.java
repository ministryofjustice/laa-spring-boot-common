package uk.gov.laa.ccms.springboot.auth;

/**
 * Holds a list of URIs available for a given role.
 *
 * @param name the name of the role
 * @param URIs the URIs that are accessible to clients that have this role
 */
public record AuthorizedRole(String name, String[] URIs) { }
