# LAA SpringBoot OAuth2 Starter

Provides OAuth2 resource-server authentication with endpoint authorization driven by
role and scope mappings.

## Usage

```groovy
dependencies {
    implementation 'uk.gov.laa.springboot:laa-spring-boot-starter-oauth2'
}
```

Configure JWT validation using standard Spring properties:

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: https://issuer.example
```

Or configure the starter to accept JWTs from multiple trusted tenants:

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
