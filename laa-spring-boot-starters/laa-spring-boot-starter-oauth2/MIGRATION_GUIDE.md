# Migrating from API Key Auth to OAuth2

This guide is for applications moving from `laa-spring-boot-starter-auth` API key
authentication to `laa-spring-boot-starter-oauth2`.

## Recommended approach

Use a versioned endpoint migration:

- keep existing API key endpoints under `/api/v1/**`
- add OAuth2 endpoints under `/api/v2/**`
- migrate consumers from v1 to v2
- remove v1 and the API key starter once all consumers use OAuth2

The version numbers are examples. If an application is currently on `/api/v5/**`, the same approach
can be used by keeping v5 on API keys and introducing OAuth2 on `/api/v6/**`.

This keeps the authentication boundary explicit and avoids trying to secure the same endpoint with
two different starter-managed security chains.

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

If both starters are present during migration, the application should define path-specific
`SecurityFilterChain` beans so that `/api/v1/**` uses API key authentication and `/api/v2/**` uses
OAuth2 authentication. The starters' default security chains back off when the application provides
its own `SecurityFilterChain` bean.

## Example configuration shape

Use explicit versions in authorization mappings during migration. Avoid `/api/v*/**` because it can
accidentally authorize both old and new endpoints.

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
            "/api/v1/**"
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
            {"method":["GET","POST","PATCH"],"uri":"/api/v2/books/**"},
            {"method":["GET","POST"],"uri":"/api/v2/authors/**"}
          ]
        }
      ]'
      authorized-scopes: '[
        {
          "name": "books.read",
          "uris": [
            {"method":["GET"],"uri":"/api/v2/books"},
            {"method":["GET"],"uri":"/api/v2/books/**"},
            {"method":["GET"],"uri":"/api/v2/authors/**"}
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

## Business-level authorization

The OAuth2 starter applies endpoint-level authorization by URI and HTTP method. For scope-based
delegated user access, any additional business-level authorization, such as checking whether the
signed-in user can access a specific record, must be implemented separately in the application.

For app-only M2M access, the role normally authorizes the calling application rather than an
individual user.
