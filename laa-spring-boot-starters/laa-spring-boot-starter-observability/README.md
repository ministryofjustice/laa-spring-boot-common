# LAA Spring Boot Starter - Observability

This Spring Boot starter provides automatic configuration for Elastic Common Schema (ECS) logging, enabling structured
logging with service metadata for observability.

## Overview

The `ObservabilityAutoConfiguration` class automatically configures ECS logging when enabled, providing structured JSON
logs that include service metadata such as service name, version, environment, and process ID.

## Features

- **ECS Logging**: Configures Logback to output logs in Elastic Common Schema format
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
logging:
  structured:
    format:
      console: ecs
    ecs:
      service:
        name: ${SPRING_APPLICATION_NAME:default}
        version: ${APP_VERSION:default}
        environment: ${SPRING_PROFILES_ACTIVE:default}

laa:
  springboot:
    starter:
      observability:
        enabled: true
```

### logback-spring.xml
The presence of this file will conflict and will take precedence over ecs structured logging.

For local/developement rename logback-local-spring.xml

set in application-local.yml

```yaml
logging:
  config: classpath:logback-local-spring.xml
```

## Usage

Once configured, all log output will be automatically formatted in ECS JSON format. Example log output:

```json
{
  "@timestamp": "2026-03-04T10:30:46.421924Z",
  "log": {
    "level": "INFO",
    "logger": "uk.gov.justice.laa.fee.scheme.controller.FeeCalculationController"
  },
  "process": {
    "pid": 55107,
    "thread": {
      "name": "http-nio-8085-exec-1"
    }
  },
  "service": {
    "name": "laa-fee-scheme-api",
    "version": "1.0.0",
    "environment": "local",
    "node": {
      "name": "banana"
    }
  },
  "message": "Successfully retrieved fee calculation",
  "transaction": {
    "id": "124918be1267b4ca"
  },
  "trace": {
    "id": "faf25bb96fd10c069fadf498fffd32df"
  },
  "label": {
    "startDate": "2020-09-30",
    "feeCode": "EDUFIN",
    "correlationId": "c2096ee6-367c-43f9-a728-dcbf14bcbe8b"
  },
  "ecs": {
    "version": "8.11"
  }
}
```

## Auto-configuration

The starter uses Spring Boot's auto-configuration mechanism:

- Activates only when `laa.springboot.starter.observability.enabled=true`

## Integration

To use this starter in your Spring Boot application:

1. Add the dependency to your `build.gradle`
2. Configure the required properties in `application.yml`
3. Start logging normally - the output will automatically be formatted in ECS
