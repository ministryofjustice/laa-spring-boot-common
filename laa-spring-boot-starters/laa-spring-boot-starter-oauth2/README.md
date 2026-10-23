# LAA SpringBoot OAuth2 Starter

Provides OAuth2 resource-server authentication with endpoint authorization driven by
role and scope mappings.

## Prerequisites

Before implementing this starter, make sure the required Microsoft Entra app registrations, app
roles and delegated scopes have already been created. The starter can only validate and authorize
roles and scopes that Entra is configured to issue in access tokens.

## Usage

```groovy
dependencies {
    implementation 'uk.gov.justice.service.laa:laa-spring-boot-starter-oauth2'
}
```

Configure the starter to accept JWTs from trusted tenants. Use the same `tenants` list for both
single-tenant and multi-tenant services:

```yaml
laa:
  springboot.starter:
    oauth2:
      resourceserver:
        jwt:
          tenants:
            - issuer-uri: https://issuer-one.example
              audiences:
                - api://example-audience
            - issuer-uri: https://issuer-two.example
              audiences:
                - api://example-audience
```

The starter uses Spring Security's servlet `JwtIssuerAuthenticationManagerResolver` to select the
tenant by JWT issuer, then validates the tenant's issuer, signature and accepted audiences.

Services can still provide standard Spring resource-server configuration or a custom `JwtDecoder`
when they need to override the starter's tenant resolver, but the starter-owned tenant config is the
recommended path.

## Choosing an example

The `examples` directory contains starter configurations for the common migration shapes:

- `scopes-only.yml` - use when only a UI calls your API, and the UI uses delegated access. In this
  model the access token contains scopes, and endpoint authorization is configured with
  `authorized-scopes`.
- `roles-only.yml` - use for application M2M access, where a backend service, queue consumer,
  scheduled job or Lambda-style workload calls your API without a signed-in user. In this model the
  access token contains app roles, and endpoint authorization is configured with `authorized-roles`.
  Some teams may temporarily use roles for a UI calling an API while delegated authorization support
  is being added to the application.
- `roles-and-scopes.yml` - use when both a UI and backend services call your API. Configure UI
  delegated permissions under `authorized-scopes`, and backend application permissions under
  `authorized-roles`.
- `multi-tenant.yml` - use only when your API must accept tokens from more than one Entra tenant.
  For example, this may be needed when an application from an external MOJ tenant needs to call your
  API. Single-tenant configuration should remain the default unless there is a clear cross-tenant
  requirement.

## Migrating from auth to oauth2 starter

See the [migration guide](MIGRATION_GUIDE.md) for guidance on migrating an existing API from
`laa-spring-boot-starter-auth` API key authentication to OAuth2. During migration, both starters can
protect the same endpoint paths. Bearer-token requests are handled by the OAuth2 starter, and
non-bearer authentication-header values continue to be handled by the API key starter.

Configure endpoint authorization mappings:

```yaml
laa.springboot.starter.oauth2:
  authorized-roles: '[
      {
          "name": "GROUP1",
          "uris": [
              "/resource1/requires-group1-role/**"
          ]
      }
  ]'
  authorized-scopes: '[
      {
          "name": "claims:read",
          "uris": [
              {
                  "method": ["GET"],
                  "uri": "/resource1/scope-only/**"
              }
          ]
      }
  ]'
  unprotected-uris: ["/actuator/**"]
```

Any configured roles and scopes must also be defined on the corresponding Microsoft Entra app
registration. The starter authorizes requests from the `roles` and scope claims in the access token;
Entra will only include those claims when the app registration exposes the roles/scopes and the
calling application has been assigned or granted them.

Keep the roles and scopes configured in each application's YAML or AWS Secrets Manager values in
line with the roles and scopes created in Entra. If Entra issues a valid token containing a role or
scope that is not configured for the requested endpoint, the token can still authenticate
successfully, but endpoint authorization will fail with access denied.

These mappings only apply endpoint-level authorization: they decide whether a caller can access a
URI and HTTP method. For scope-based delegated user access, any additional business-level
authorization, such as checking whether the signed-in user can access a specific record, library,
author or book, must be implemented separately in the application. For app-only M2M access, the
role normally authorizes the calling application rather than an individual user.

## Adding security filters

Applications can add filters to the OAuth2 security chain with
`laa.springboot.starter.oauth2.additionalSecurityFilters`. Supply an ordered list of fully
qualified filter class names. Each class must have exactly one corresponding Spring `Filter` bean
in the application context; the starter does not construct filters on the application's behalf.

Additional filters run after bearer-token authentication and before endpoint authorization. They
therefore have access to the authenticated `JwtAuthenticationToken`, and changes they make to its
authorities are available to `authorized-roles` and `authorized-scopes` checks. List order is
execution order.

```yaml
laa.springboot.starter.oauth2:
  additionalSecurityFilters:
    - com.example.security.RequestEntitlementFilter
    - com.example.security.RequestAuditFilter
```

For example, the application must provide the selected filter as a Spring bean:

```java
@Configuration
class SecurityFilterConfiguration {

  @Bean
  RequestEntitlementFilter requestEntitlementFilter(EntitlementService entitlementService) {
    return new RequestEntitlementFilter(entitlementService);
  }
}
```

The application fails at startup when an entry is blank, cannot be loaded, is not a servlet
`Filter`, or has zero or more than one matching Spring beans. This avoids silently starting
without an expected security filter.

## Using X-Authorization

`XAuthorizationFilter` is an optional additional filter for a supplementary raw JWT sent in the
`X-Authorization` header. It validates the supplementary token, requires its Entra `oid` to match
the authenticated bearer token's `oid`, then adds its `LAA_APP_ROLES` claim to the authenticated
principal. Those roles use the configured `role-authority-prefix` (default `ROLE_`) and can be
used in `authorized-roles` mappings.

To enable it, explicitly import `XAuthorizationConfig` in the application and list the filter:

```java
@Configuration
@Import(XAuthorizationConfig.class)
class SecurityFilterConfiguration {
}
```

```yaml
laa.springboot.starter.oauth2:
  additionalSecurityFilters:
    - uk.gov.laa.springboot.oauth2.XAuthorizationFilter

spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: https://login.microsoftonline.com/<tenant-id>/v2.0
          jwk-set-uri: https://login.microsoftonline.com/<tenant-id>/discovery/v2.0/keys
```

The `X-Authorization` value must be the raw compact JWT, not `Bearer <token>`. The header is
optional: without it, the normal bearer token continues through authorization unchanged. A supplied
X-Authorization token that is invalid, is not JWT-backed by the primary request authentication, or
has a different `oid` receives `401 Unauthorized`.

## Mocking auth in tests

The starter includes `StubJwtDecoder` and `StubJwtToken` test helpers under:
`uk.gov.laa.springboot.oauth2.testsupport`.

Example test configuration:

```java
@TestConfiguration
class TestJwtConfig {
  @Bean
  JwtDecoder jwtDecoder() {
    return StubJwtDecoder.of(
        new StubJwtToken("token-group1", "client-a", new String[]{"GROUP1"}, null, Map.of()),
        new StubJwtToken("token-scope", "client-b", null,
            new String[]{"claims:read"}, Map.of()));
  }
}
```
