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

From the example above it would generate the following:

![swagger-ui generated from export starter](examples/GeneratedSwagger/library_books.png)

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
