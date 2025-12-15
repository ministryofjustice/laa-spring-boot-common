# LAA Spring Boot Starter – SQL Scanner

Adds an aspect that inspects controller arguments **and repository save/update entities** for SQL-like
patterns and logs a warning when found.
Annotate classes, individual fields/record components, or method parameters with `@ScanForSql` to opt in.

The scanner safely walks nested structures, collections, arrays, and maps, while skipping JDK
types (e.g., `java.util.UUID`) to avoid illegal reflection.

---

## Declare the dependency

```groovy
dependencies {
    implementation "uk.gov.laa.springboot:laa-spring-boot-starter-sql-scanner"
    implementation "org.springframework.boot:spring-boot-starter-aop" // required for aspects
}
```

---

## Usage

### Controller Method Scanning

* Scans all arguments of `@RestController` and `@Controller` methods.
* If an argument **type** is annotated with `@ScanForSql`, the entire object graph is scanned.
* If only specific fields/record components are annotated, only those values are scanned.
* Method parameters annotated directly with `@ScanForSql` are also scanned.
* Patterns checked by default: `select`, `insert`, `update`, `delete`, `drop`, `truncate`, `union`,
  SQL comment markers, and `or/and` comparisons.
* JDK/platform types are skipped automatically.

---

## Repository save/update scanning

The starter also scans repository operations:

* Applies to any `@Repository` or Spring Data repository method starting with
  **`save*` or `update*`**
* Scanning occurs when:

    * the **entity class** is annotated with `@ScanForSql`, **or**
    * **any field/record component** inside the entity is annotated with `@ScanForSql`

This gives fine-grained, opt-in scanning while still allowing selective field-level scanning.

### Examples

#### 1. Full entity scanning (record/class-level annotation)

```java
@ScanForSql
public record CreateCustomerRequest(
    String name,
    String email,
    String comments
) {}
```

```java
@ScanForSql
public class CustomerEntity {
    String notes; // scanned
}
```

Saving/updating will scan the entire entity graph:

```java
repository.save(new CustomerEntity());
```

#### 2. Selective scanning (field-level annotation)

```java
public record FeedbackRequest(
    String name,
    @ScanForSql String message // only this field is scanned
) {}
```

```java
public class PaymentEntity {
    @ScanForSql String comment; // scanned
    String code;                // not scanned
}
```

This WILL trigger scanning of **only the annotated fields**:

```java
repository.updatePayment(new PaymentEntity());
```

#### 3. No annotation → no scanning

```java
public class ReportEntity {
    String text; // NOT scanned
}
```

```java
repository.save(reportEntity); // ignored completely
```
---

## Ignoring classes during scanning

`@ScanForSql` supports an `ignoreClasses` attribute to completely exclude specific types from scanning within the annotated scope.

### Behaviour

* Ignored classes are **not scanned or traversed**
* Their fields, record components, and nested objects are skipped entirely
* Applies to controller arguments and repository save/update scanning
* Useful for large, safe, or third-party types

### Example

```java
@ScanForSql(ignoreClasses = AuditMetadata.class)
public record CreateOrderRequest(
    String description,
    AuditMetadata auditMetadata
) {}
```

`AuditMetadata` and anything inside it will be skipped during scanning.

---

## Logging behaviour

On detection:
The aspect logs at `WARN` with the field/component name and the matched pattern so that requests can
be flagged or rejected upstream.

```
WARN Suspicious SQL-like pattern 'drop' in field 'payment.comment': 'drop table x'
```

The message includes:

* matched pattern
* full nested field path
* offending value

---

## Missing AOP

If Spring AOP is absent, the starter logs a warning at startup and skips scanning until
`spring-boot-starter-aop` is added to the classpath.

---
