# LAA SpringBoot OAuth2 Starter

Provides OAuth2 resource-server authentication with endpoint authorization driven by
role and scope mappings.

## Usage

```groovy
dependencies {
    implementation 'uk.gov.laa.springboot:laa-spring-boot-starter-oauth2'
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
