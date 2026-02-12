# LAA Spring Boot Starter - Exports (CSV Only)

Provides a supported CSV export framework for LAA Spring Boot services, including:

- runtime export orchestration (`ExportService`, `ExportRegistry`, `ExportRequestValidator`, `ExportAuditSink`)
- PostgreSQL `COPY ... TO STDOUT` export support
- Gradle code generation for export providers and REST controllers

## Dependency

```groovy
dependencies {
    implementation "uk.gov.laa.springboot:laa-spring-boot-starter-export"
}
```

## Configuration

```yaml
laa:
  springboot:
    starter:
      exports:
        enabled: true
        defaults:
          max-rows: 50000
        web:
          base-path: /exports
        definitions:
          library_books:
            provider: libraryBooksProvider
            packageName: uk.gov.laa.springboot.export.generated
            sql: |
              select id as book_id, status as status from library_books where id > :minId
            columns:
              - key: book_id
                header: Book ID
              - key: status
                header: Status
            params:
              - name: minId
                type: LONG
                required: false
```

## Export Definitions

Definitions can be supplied in either:

- `src/main/resources/application.yml` under `laa.springboot.starter.exports.definitions`
- one or more files in `src/main/resources/export_definitions/*.yml` or `*.yaml`

Supported param types are `STRING`, `UUID`, `INT`, `LONG`, `BOOLEAN`, `DATE`, and `ENUM`.

Example definition files are available in:

- `examples/export_definitions/library_books_basic.yml`
- `examples/export_definitions/library_books_with_authors.yml`
- `examples/export_definitions/library_overdue_loans.yml`
- `examples/export_definitions/library_monthly_checkout_totals.yml`
- `examples/export_definitions/library_books_optional_filters.yml`

## Generated Code

SQL code generation is automatically enabled when both are true:

- your project applies `uk.gov.laa.springboot.laa-spring-boot-gradle-plugin`
- your project depends on `uk.gov.laa.springboot:laa-spring-boot-starter-export`

This provides:

- `generateExportSql`: generates `ExportCsvProvider` classes from SQL definitions
- `generateExportControllers`: generates REST CSV controller classes from export definitions

Both tasks read from `application.yml` and `export_definitions/*.yml|*.yaml`.

Default generated package is:

- `uk.gov.laa.springboot.export.generated`

Override per definition with:

- `packageName`

## Generated Endpoints

Generated controllers expose CSV endpoints at:

- `${laa.springboot.starter.exports.web.base-path:/exports}/{exportKey}.csv`

Request params are mapped from definition `params`, and generated OpenAPI annotations include CSV header examples when column metadata is available.
Generated filenames include the export key, non-empty request params (in definition order), and current date:

- `{exportKey}-{param1}-{param2}-...-{yyyy-MM-dd}.csv`

From the example above it would generate the following:

![swagger-ui generated from export starter](examples/GeneratedSwagger/library_books.png)

## Auditing

By default, export events are logged by `uk.gov.laa.springboot.export.audit.LogExportAuditSink`.

Example success event:

```text
u.g.l.s.export.audit.LogExportAuditSink : export_success key=library_books rows=3 maxRows=50000 durationMs=48 startedAt=2026-02-12T13:13:39.824Z
```

Example failure event:

```text
u.g.l.s.export.audit.LogExportAuditSink : export_failed key=library_books rows=0 maxRows=50000 durationMs=12 startedAt=2026-02-12T13:13:39.824Z error=Filter minId must be a long
```

To customize this behavior, define your own `ExportAuditSink` bean.

## Error Handling

Validation failures are handled by `uk.gov.laa.springboot.export.config.ExportExceptionHandler`.
When an `ExportValidationException` is thrown, the starter returns `400 Bad Request` as a
`ProblemDetail` response with the validation message in the `detail` field.

## Troubleshooting

### Startup error: missing provider bean

Example:

- `Method exportRegistry ... required a bean named 'libraryBooksProvider' that could not be found`

Cause:

- generated provider classes are created in `uk.gov.laa.springboot.export.generated` by default
- if your `@SpringBootApplication` base package does not include that package, Spring will not discover the generated `@Component` bean

Recommended fix:

- set `packageName` on each export definition to a package under your service base package (for example `uk.gov.justice.laa.yourservice.export.generated`)

Alternative fix:

- widen component scanning with `@SpringBootApplication(scanBasePackages = "...")` to include the generated package

### Checkstyle and JaCoCo on generated classes

Generated sources (for example under `build/generated/export-sql` and `build/generated/export-web`) should usually be excluded from static analysis and coverage rules.

Example Gradle configuration:

```groovy
tasks.withType(Checkstyle).configureEach {
    exclude "**/build/generated/**"
    exclude "**/generated/**"
}

jacocoTestReport {
    afterEvaluate {
        classDirectories.setFrom(files(classDirectories.files.collect {
            fileTree(dir: it, exclude: [
                "**/generated/**",
                "**/*Generated*.*",
                "**/export/generated/**"
            ])
        }))
    }
}

jacocoTestCoverageVerification {
    afterEvaluate {
        classDirectories.setFrom(files(classDirectories.files.collect {
            fileTree(dir: it, exclude: [
                "**/generated/**",
                "**/*Generated*.*",
                "**/export/generated/**"
            ])
        }))
    }
}
```
