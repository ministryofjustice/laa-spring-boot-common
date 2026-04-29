package uk.gov.laa.springboot.oauth2;

import com.fasterxml.jackson.annotation.JsonFormat;
import tools.jackson.databind.annotation.JsonDeserialize;

/**
 * Holds URI permissions for one role or scope name.
 *
 * @param name authority name without prefix
 * @param uris URI patterns the authority can access
 */
@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
public record AuthorizedAuthority(
    String name,
    @JsonDeserialize(contentUsing = AuthorizedRoleUriDeserializer.class)
    AuthorizedRoleUri[] uris) {
}
