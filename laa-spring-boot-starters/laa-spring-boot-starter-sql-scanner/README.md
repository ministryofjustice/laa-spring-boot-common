# LAA Spring Boot Starter – SQL Scanner

Adds an aspect that inspects controller arguments for SQL-like patterns and logs a warning when
found. Annotate classes or individual fields/record components with `@ScanForSql` to opt in.

## Declare the dependency

```groovy
dependencies {
    implementation "uk.gov.laa.springboot:laa-spring-boot-starter-sql-scanner"
    implementation "org.springframework.boot:spring-boot-starter-aop" // required for aspects
}
```

## Usage

- The starter hooks into every method on `@RestController` and `@Controller` beans.
- If an argument's type is annotated with `@ScanForSql`, every `String` field/record component is
  scanned.
- If only specific fields/components are annotated, only those values are scanned.
- Patterns checked by default: `select`, `insert`, `update`, `delete`, `drop`, `truncate`, `union`,
  SQL comment markers, and `or/and` comparisons.

```java
public record FeedbackRequest(
    String name,
    @ScanForSql String message // only this field is scanned
) {}

@ScanForSql
public record CreateCustomerRequest(
    String name,
    String email,
    String comments
) {}
```

The aspect logs at `WARN` with the field/component name and the matched pattern so that requests can
be flagged or rejected upstream.

If Spring AOP is absent, the starter logs a warning at startup and skips scanning until
`spring-boot-starter-aop` is added to the classpath.
