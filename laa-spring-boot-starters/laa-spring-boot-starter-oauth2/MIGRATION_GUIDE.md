# Migrating from API Key Auth to OAuth2

This guide is for applications moving from `laa-spring-boot-starter-auth` API key
authentication to `laa-spring-boot-starter-oauth2`.

## Recommended approach

Use a shared-endpoint migration:

- keep the existing endpoint paths
- add the OAuth2 starter alongside the API key starter
- configure the same endpoint paths under both starters during migration
- migrate consumers from API keys to bearer tokens
- remove the API key starter and API key configuration once all consumers use OAuth2

When both starters are present, the starters route requests by the shape of the configured
authentication header:

- `Authorization: Bearer <jwt>` uses OAuth2 resource-server authentication
- `Authorization: <api-key>` uses API key authentication

This means applications do not need to create duplicate endpoint versions purely to migrate
authentication. Versioned endpoint migration is still valid when the API contract itself is also
changing.

## Before starting

Make sure the Microsoft Entra setup exists before changing the application:

- app registration for the API
- app roles for application M2M callers
- delegated scopes for UI/delegated callers
- granted permissions or role assignments for calling applications
- tenant issuer and audience values for the API

The OAuth2 starter only validates and authorizes roles and scopes that Entra is already configured
to issue in access tokens. It does not implement the OAuth On-Behalf-Of flow.

## Choosing OAuth2 roles or scopes

Use `authorized-scopes` for delegated access where a UI or delegated caller is acting with a
signed-in user.

Use `authorized-roles` for app-only M2M access, such as backend services, queue consumers,
scheduled jobs or Lambda-style workloads.

Use both when the API is called by both UIs and backend services.

## Running both starters during migration

Do not rely on `unprotected-uris` in one starter to pass requests to the other starter.
Unprotected URIs are permitted within the selected Spring Security chain; they do not cause Spring
Security to try another chain.

The starters provide ordered security chains for the migration case. The OAuth2 chain runs first
and matches bearer-token requests. The API key chain runs after it and matches non-bearer requests.

Applications can still provide their own `SecurityFilterChain` beans for unusual routing needs. To
replace a starter-managed chain, define a bean with the relevant starter bean name:

- `oauth2SecurityFilterChain`
- `apiKeySecurityFilterChain`

## Example configuration shape

Configure the same endpoint paths in both starters while consumers are migrating. Keep the API key
role/client mapping for existing callers, and add OAuth2 role or scope mappings for migrated
callers.

```yaml
laa:
  springboot.starter:
    auth:
      authentication-header: "Authorization"
      authorized-clients: '[
        {
          "name": "test-runner",
          "roles": ["ALL"],
          "token": "<api_key_token>"
        }
      ]'
      authorized-roles: '[
        {
          "name": "ALL",
          "uris": [
            "/api/v1/books/**",
            "/api/v1/authors/**"
          ]
        }
      ]'
      unprotected-uris: [
        "/actuator/**",
        "/swagger-ui.html",
        "/swagger-ui/**",
        "/v3/api-docs/**",
        "/favicon.ico",
        "/open-api-specification.yml"
      ]

    oauth2:
      resourceserver:
        jwt:
          tenants:
            - issuer-uri: https://login.microsoftonline.com/<tenant_id>/v2.0
              audiences:
                - <entra_app_registration_client_id>
      authorized-roles: '[
        {
          "name": "library-catalogue-service",
          "uris": [
            {"method":["GET","POST","PATCH"],"uri":"/api/v1/books/**"},
            {"method":["GET","POST"],"uri":"/api/v1/authors/**"}
          ]
        }
      ]'
      authorized-scopes: '[
        {
          "name": "books.read",
          "uris": [
            {"method":["GET"],"uri":"/api/v1/books"},
            {"method":["GET"],"uri":"/api/v1/books/**"},
            {"method":["GET"],"uri":"/api/v1/authors/**"}
          ]
        }
      ]'
      unprotected-uris: [
        "/actuator/**",
        "/swagger-ui.html",
        "/swagger-ui/**",
        "/v3/api-docs/**",
        "/favicon.ico",
        "/open-api-specification.yml"
      ]
```

Existing API key callers continue to send:

```http
Authorization: <api_key_token>
```

Migrated OAuth2 callers send:

```http
Authorization: Bearer <jwt>
```

## Business-level authorization

The OAuth2 starter applies endpoint-level authorization by URI and HTTP method. For scope-based
delegated user access, any additional business-level authorization, such as checking whether the
signed-in user can access a specific record, must be implemented separately in the application.

For app-only M2M access, the role normally authorizes the calling application rather than an
individual user.
