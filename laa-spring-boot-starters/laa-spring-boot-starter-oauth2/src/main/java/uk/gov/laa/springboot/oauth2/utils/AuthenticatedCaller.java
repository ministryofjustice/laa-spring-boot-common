package uk.gov.laa.springboot.oauth2.utils;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.util.StringUtils;

/** Immutable, minimum-necessary projection of the authenticated caller's validated token claims. */
public record AuthenticatedCaller(
    UUID actorId,
    String profileId,
    String firmCode,
    Set<String> delegatedScopes,
    Set<String> providerOfficeCodes,
    Set<String> applicationRoles
) {}
