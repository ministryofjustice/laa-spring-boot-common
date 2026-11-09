package uk.gov.laa.springboot.auth;

import com.fasterxml.jackson.annotation.JsonFormat;
import tools.jackson.databind.annotation.JsonDeserialize;

/**
 * Holds a list of uris available for a given role.
 *
 * @param name the name of the role
 * @param uris the uris that are accessible to clients that have this role
 * @deprecated Prefer {@code laa-spring-boot-starter-oauth2} for new API integrations. See its
 *     README and migration guide for setup guidance.
 */
@Deprecated
@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
public record AuthorizedRole(
    String name,
    @JsonDeserialize(contentUsing = AuthorizedRoleUriDeserializer.class)
    AuthorizedRoleUri[] uris) {}
