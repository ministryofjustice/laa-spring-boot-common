# LAA Spring Boot Starter - Observability

This Spring Boot starter provides automatic configuration for Elastic Common Schema (ECS) logging, enabling structured
logging with service metadata for observability.

## Overview

The `ObservabilityAutoConfiguration` class automatically configures ECS logging when enabled, providing structured JSON
logs that include service metadata such as service name, version, environment, and process ID.

## Features

- **ECS Logging**: Configures Logback to output logs in Elastic Common Schema format
- **Service Metadata**: Automatically adds service name, version, and environment to log entries
- **Process ID**: Includes the process ID in log entries for better tracing
- **Conditional Activation**: Only activates when explicitly enabled via configuration
- **Tracing filter MDC**: Trace ID/Transaction ID Management: Extracts or generates from:
1. Request header trace.id/transaction.id
2. Spring's MDC traceId/spanId
3. Generates a random UUID if neither exists

## Configuration

Add the starter to your Spring Boot application and configure the following properties:

```groovy
dependencies {
    implementation "uk.gov.laa.springboot:laa-spring-boot-starter-observability"
}
```

```yaml
laa:
  springboot:
    starter:
      observability:
        enabled: true
        service-name: ${SPRING_APPLICATION_NAME:default}
        service-version: ${APP_VERSION:default}
        environment: ${SPRING_PROFILES_ACTIVE:default}
```

## Usage

Once configured, all log output will be automatically formatted in ECS JSON format. Example log output:

```json
{
  "@timestamp": "2026-03-02T16:39:13.176Z",
  "log.level": "INFO",
  "message": "Successfully retrieved fee calculation",
  "ecs.version": "1.2.0",
  "service.name": "laa-fee-scheme-api",
  "service.version": "1.0.0",
  "service.environment": "local",
  "event.dataset": "laa-fee-scheme-api",
  "process.thread.name": "http-nio-8085-exec-4",
  "log.logger": "uk.gov.justice.laa.fee.scheme.controller.FeeCalculationController",
  "process.pid": "7289",
  "traceId": "e6b15cf16eae9c7bf988b3be676c632b",
  "spanId": "6de8bb9fbf899f7c"
}
```

## Auto-configuration

The starter uses Spring Boot's auto-configuration mechanism:

- Activates only when `laa.springboot.starter.observability.enabled=true`
- Configures ECS encoder with service metadata
- Replaces all existing console appenders with ECS-formatted console appender
- Automatically adds process ID to all log entries

## Integration

To use this starter in your Spring Boot application:

1. Add the dependency to your `build.gradle`
2. Configure the required properties in `application.yml`
3. Start logging normally - the output will automatically be formatted in ECS
