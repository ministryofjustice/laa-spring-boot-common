package uk.gov.laa.springboot.slack;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

/**
 * Sends formatted Slack notifications via an incoming webhook.
 */
public class SlackNotifier {

  private static final Logger LOG = LoggerFactory.getLogger(SlackNotifier.class);
  private static final ZoneId UK_ZONE = ZoneId.of("Europe/London");
  private static final DateTimeFormatter TIMESTAMP_FORMATTER =
      DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ssXXX");

  private final boolean notificationsEnabled;
  private final String webhook;
  private final String environmentName;
  private final String namespace;
  private final String serverDetails;
  private final Clock clock;

  public SlackNotifier(
      SlackNotificationProperties properties, Environment environment) {
    this(properties, environment, Clock.system(UK_ZONE));
  }

  SlackNotifier(
      SlackNotificationProperties properties,
      Environment environment,
      Clock clock) {

    this.notificationsEnabled = properties.isEnabled() && properties.hasWebhook();
    this.webhook = properties.getWebhook();
    this.clock = clock;

    this.namespace = ensureNamespace(resolveNamespace(properties, environment));
    String podName = resolvePodName(properties, environment);
    String podIp = resolvePodIp(properties, environment);
    this.serverDetails = buildServerDetails(podName, podIp);
    this.environmentName = resolveEnvironmentName(properties, namespace);

    if (notificationsEnabled) {
      LOG.info("Slack notifications are enabled.");
    } else {
      LOG.info("Slack notifications are disabled (missing webhook or disabled flag).");
    }
  }

  /**
   * Sends a notification to Slack using the configured webhook.
   *
   * @param message main alert message
   * @param extraInfo optional additional information rendered in a preformatted block
   * @param icon icon to display alongside the message
   */
  public void sendSlackNotification(String message, String extraInfo, SlackIcon icon) {
    if (!notificationsEnabled) {
      LOG.debug("Skipping Slack notification because notifications are disabled.");
      return;
    }

    if (!StringUtils.hasText(message)) {
      LOG.warn("Skipping Slack notification because message is blank.");
      return;
    }

    SlackIcon resolvedIcon = (icon != null) ? icon : SlackIcon.INFORMATION;
    String timestamp = ZonedDateTime.now(clock).format(TIMESTAMP_FORMATTER);
    String headline = "Environment " + environmentName + " : " + message;
    String payload =
        SlackMessageFormatter.formatMessage(
            resolvedIcon.toString(), headline, extraInfo, namespace, serverDetails, timestamp);

    postPayload(payload);
  }

  private void postPayload(String payload) {
    HttpURLConnection connection = null;
    try {
      URL url = new URL(webhook);
      connection = openConnection(url);
      connection.setDoOutput(true);
      connection.setRequestMethod("POST");
      connection.setRequestProperty("Content-Type", "application/json");
      connection.setRequestProperty("Accept", "application/json");

      try (OutputStream os = connection.getOutputStream()) {
        byte[] input = payload.getBytes(StandardCharsets.UTF_8);
        os.write(input, 0, input.length);
      }

      int responseCode = connection.getResponseCode();
      if (responseCode != HttpURLConnection.HTTP_OK) {
        LOG.warn(
            "Slack notification returned non-success response code [{}].", responseCode);
      }
    } catch (IOException ex) {
      LOG.error("Failed to send Slack notification.", ex);
    } finally {
      if (connection != null) {
        connection.disconnect();
      }
    }
  }

  HttpURLConnection openConnection(URL url) throws IOException {
    return (HttpURLConnection) url.openConnection();
  }

  private static String ensureNamespace(String namespace) {
    return (StringUtils.hasText(namespace)) ? namespace : "UNKNOWN";
  }

  private static String resolveNamespace(
      SlackNotificationProperties properties, Environment environment) {
    return firstNonEmpty(
        properties.getPod().getNamespace(), environment.getProperty("POD_NAMESPACE"));
  }

  private static String resolvePodName(
      SlackNotificationProperties properties, Environment environment) {
    return firstNonEmpty(
        properties.getPod().getName(), environment.getProperty("POD_NAME"));
  }

  private static String resolvePodIp(
      SlackNotificationProperties properties, Environment environment) {
    return firstNonEmpty(
        properties.getPod().getIp(), environment.getProperty("POD_IP"));
  }

  private static String buildServerDetails(String podName, String podIp) {
    if (!StringUtils.hasText(podName) && !StringUtils.hasText(podIp)) {
      return "UNKNOWN";
    }

    if (!StringUtils.hasText(podName)) {
      return podIp;
    }

    if (!StringUtils.hasText(podIp)) {
      return podName;
    }

    return podName + " " + podIp;
  }

  private static String resolveEnvironmentName(
      SlackNotificationProperties properties, String namespace) {
    if (StringUtils.hasText(properties.getEnvironmentName())) {
      return properties.getEnvironmentName();
    }

    if (StringUtils.hasText(namespace) && !"UNKNOWN".equalsIgnoreCase(namespace)) {
      String[] parts = namespace.split("-");
      String environment = parts[parts.length - 1];
      return environment.toUpperCase(Locale.UK);
    }

    return "UNKNOWN";
  }

  private static String firstNonEmpty(String... values) {
    for (String value : values) {
      if (StringUtils.hasText(value)) {
        return value;
      }
    }
    return "";
  }
}
