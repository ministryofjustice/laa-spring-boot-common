# LAA Spring Boot Starter – Metrics

Simplifies setting up custom Prometheus metrics within your Spring Boot application. The starter
auto-cofigures multiple services to expose Prometheus metrics for your application such as:

- `MetricAnnotationScanner`
- `CounterAspect`
- `TimerAspect`
- `CounterMetricService`
- `HistogramMetricService`
- `SummaryMetricService`

## Why use this over official Prometheus/Micrometer annotations?

Standard Prometheus annotations (like `@Timed` from Micrometer) often rely on **Spring Proxying**. This means that if you call an annotated method from *within* the same class (a self-invocation), the annotation is ignored because the call doesn't pass through the Spring proxy.

This starter's aspects are designed to:

1.  **Eliminate the Proxy Limitation**: By using AspectJ-style pointcuts within our aspects, we ensure that metrics are captured even during internal method calls where standard Spring-proxied annotations would fail.
2.  **Zero-Configuration Setup**: Unlike official implementations that require you to manually register `TimedAspect` beans or configure `MeterRegistry` customizations, this starter auto-configures everything.
3.  **Domain-Driven Metrics**: Instead of technical implementation details, your code uses business-centric annotations (`@CounterMetric`) that handle the logic of value extraction and label mapping automatically.


## Usage

### 1. Declare the dependency

```groovy
dependencies {
    implementation "uk.gov.laa.springboot:laa-spring-boot-starter-metrics"
}
```

### 2. Provide configuration

```yaml
laa:
  springboot:
    starter:
      metrics:
        metric-name-prefix: ${METRIC_NAME_PREFIX}
```

### 3. Send alerts

The starter provides various annotations to simplify recording any metrics within your application.
These include:

- `@CounterMetric`
- `@HistogramMetric`
- `@SummaryTimerMetric`
- `@HistogramTimerMetric`

Metrics are then record upon successful completion of the annotated method.

```java
// Would count the 'my_counter' metric once myMethod() has finished executing
@CounterMetric(metricName = "my_counter", labels = {"label1", "label2"})
void myMethod() {
  ...
}
```

#### CounterMetric

The `@CounterMetric` annotation is used to record a single value for a metric using Prometheus'
`counter` type. Each invocation of the annotated method will increment the counter.

| Parameter             | Type     | Description                                                                                    | Default Value |
|-----------------------|----------|------------------------------------------------------------------------------------------------|---------------|
| `metricName`          | String   | The name of the metric you wish to record. Will be appended to the base metric name.           |               |
| `labels`              | String[] | Static labels in the form "key=value". Must be the same for each usage of the same metricName. |               |
| `hintText`            | String   | Hint text to describe what the metric is recording.                                            |               |
| `amount`              | double   | Increment value.                                                                               | 1.0           |
| `conditionalOnReturn` | String   | Only increments the counter if the return value matches.                                       |               |
| `saveReturnValue`     | String   | Stores the returned value within a label named `value`.                                        | false         |

#### HistogramMetric

The `@HistogramMetric` annotation is used to record a single value for a metric using Prometheus'
`histogram` type. Each invocation of the annotated method will record the value in the histogram.

| Parameter              | Type                 | Description                                                                                    | Default Value |
|------------------------|----------------------|------------------------------------------------------------------------------------------------|---------------|
| `metricName`           | String               | The name of the metric you wish to record. Will be appended to the base metric name.           |               |
| `labels`               | String[]             | Static labels in the form "key=value". Must be the same for each usage of the same metricName. |               |
| `hintText`             | String               | Hint text to describe what the metric is recording.                                            |               |
| `valueCaptureStrategy` | ValueCaptureStrategy | Defines what value should be recorded (return value, param0, param2 etc).                      | 1.0           |

#### SummaryTimerMetric

The `@SummaryTimerMetric` annotation is used to record the execution time of a method using Prometheus'
`summary` type. Each invocation of the annotated method will record the execution time in the summary.

| Parameter             | Type     | Description                                                                                    | Default Value |
|-----------------------|----------|------------------------------------------------------------------------------------------------|---------------|
| `metricName`          | String   | The name of the metric you wish to record. Will be appended to the base metric name.           |               |
| `labels`              | String[] | Static labels in the form "key=value". Must be the same for each usage of the same metricName. |               |
| `hintText`            | String   | Hint text to describe what the metric is recording.                                            |               |

#### HistogramTimerMetric

The `@HistogramTimerMetric` annotation is used to record the execution time of a method using Prometheus'
`histogram` type. Each invocation of the annotated method will record the execution time in the histogram.


| Parameter             | Type     | Description                                                                                    | Default Value |
|-----------------------|----------|------------------------------------------------------------------------------------------------|---------------|
| `metricName`          | String   | The name of the metric you wish to record. Will be appended to the base metric name.           |               |
| `labels`              | String[] | Static labels in the form "key=value". Must be the same for each usage of the same metricName. |               |
| `hintText`            | String   | Hint text to describe what the metric is recording.                                            |               |

## Testing

The starter ships with unit tests covering the metrics functionality. No additional test
setup is required when consuming the starter.
