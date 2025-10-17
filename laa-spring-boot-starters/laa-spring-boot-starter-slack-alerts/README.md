# LAA Spring Boot Starter – Slack Alerts

Simplifies sending rich Slack notifications from Spring Boot applications via an incoming webhook. The starter auto-configures an `AlertService` backed by a `SlackNotifier` that formats messages with pod metadata for observability.

## Usage

### 1. Declare the dependency

```groovy
dependencies {
    implementation "uk.gov.laa.springboot:laa-spring-boot-starter-slack-alerts"
}
```

### 2. Provide configuration

```yaml
laa:
  springboot:
    starter:
      slack-alerts:
        webhook: ${SLACK_WEBHOOK_URL}
        # optional overrides
        # enabled: true
        # environment-name: PROD
        # pod:
        #   namespace: ${POD_NAMESPACE}
        #   name: ${POD_NAME}
        #   ip: ${POD_IP}
```

The starter reads pod metadata from either the `laa.springboot.starter.slack-alerts.pod.*` properties or the standard Kubernetes
Downward API environment variables (`POD_NAMESPACE`, `POD_NAME`, `POD_IP`). If none are available the
values default to `UNKNOWN`.

To populate those environment variables in Kubernetes add the following to your deployment manifest:

```yaml
        - name: POD_NAME
          valueFrom:
            fieldRef:
              fieldPath: metadata.name
        - name: POD_NAMESPACE
          valueFrom:
            fieldRef:
              fieldPath: metadata.namespace
        - name: POD_IP
          valueFrom:
            fieldRef:
              fieldPath: status.podIP
```

### 3. Send alerts

Inject `AlertService` anywhere in your application and call one of its helper methods:

```java
alertService.sendAlertNotification("Message processed", SlackIcon.PARTY);
alertService.sendAlertNotification("Processing failed", details, SlackIcon.ERROR);
```

If Slack notifications are disabled (no webhook configured or `laa.springboot.starter.slack-alerts.enabled=false`) the service
skips sending messages while logging at debug level.

## Behaviour

- Uses the final segment of the namespace (e.g. `prod` from `my-app-prod`) as the environment name unless
  `laa.springboot.starter.slack-alerts.environment-name` is provided.
- Formats messages using Slack "blocks" with optional pre-formatted additional information.
- Sends requests using Java's `HttpURLConnection` to avoid extra dependencies.

## Testing

The starter ships with unit tests covering the auto-configuration and payload formatting. No additional
setup is required when consuming the starter.
