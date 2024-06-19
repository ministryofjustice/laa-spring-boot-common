package uk.gov.laa.ccms.springboot.auth;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * Holds a list of uris available for a given role.
 *
 * @param name the name of the role
 * @param uris the uris that are accessible to clients that have this role
 */
@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
public record AuthorizedRole(String name, String[] uris) {}
