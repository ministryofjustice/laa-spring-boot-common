package uk.gov.laa.springboot.slack;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

class SlackNotifierTest {

  private static final Clock FIXED_CLOCK =
      Clock.fixed(Instant.parse("2024-01-01T09:30:00Z"), ZoneId.of("Europe/London"));

  @Test
  void skipsSendingWhenWebhookMissing() {
    SlackNotificationProperties properties = new SlackNotificationProperties();
    MockEnvironment environment = new MockEnvironment();

    TestSlackNotifier notifier = new TestSlackNotifier(properties, environment, FIXED_CLOCK);
    notifier.sendSlackNotification("message", null, null);

    assertThat(notifier.isSent()).isFalse();
  }

  @Test
  void buildsExpectedPayloadAndSendsNotification() {
    SlackNotificationProperties properties = new SlackNotificationProperties();
    properties.setWebhook("https://hooks.slack.test/123");

    MockEnvironment environment =
        new MockEnvironment()
            .withProperty("POD_NAMESPACE", "laa-application-prod")
            .withProperty("POD_NAME", "laa-application-1234567890")
            .withProperty("POD_IP", "10.0.0.5");

    TestSlackNotifier notifier = new TestSlackNotifier(properties, environment, FIXED_CLOCK);
    notifier.sendSlackNotification("Notification processed", "Payload details", SlackIcon.PARTY);

    assertThat(notifier.isSent()).isTrue();
    assertThat(notifier.getPayload())
        .contains("\"name\": \"party\"")
        .contains("Environment PROD : Notification processed")
        .contains("\"text\": \"Namespace: \"")
        .contains("\"text\": \"laa-application-prod\"")
        .contains("\"text\": \"Server: \"")
        .contains("\"text\": \"laa-application-1234567890 10.0.0.5\"")
        .contains("\"text\": \"Time: \"")
        .contains("2024-01-01 09:30:00Z")
        .contains("\"text\": \"Additional Information : \"")
        .contains("\"text\": \"Payload details\"");
  }

  private static final class TestSlackNotifier extends SlackNotifier {
    private CapturingConnection connection;
    private boolean sent;

    TestSlackNotifier(
        SlackNotificationProperties properties, MockEnvironment environment, Clock clock) {
      super(properties, environment, clock);
    }

    @Override
    HttpURLConnection openConnection(URL url) throws IOException {
      sent = true;
      connection = new CapturingConnection(url);
      return connection;
    }

    boolean isSent() {
      return sent;
    }

    String getPayload() {
      return (connection != null) ? connection.getPayload() : null;
    }
  }

  private static final class CapturingConnection extends HttpURLConnection {

    private final ByteArrayOutputStream captured = new ByteArrayOutputStream();

    CapturingConnection(URL url) {
      super(url);
    }

    @Override
    public void disconnect() {}

    @Override
    public boolean usingProxy() {
      return false;
    }

    @Override
    public void connect() throws IOException {}

    @Override
    public void setRequestMethod(String method) throws ProtocolException {
      // Allow all methods
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
      return new FilterOutputStream(captured) {
        @Override
        public void close() throws IOException {
          super.close();
        }
      };
    }

    @Override
    public int getResponseCode() {
      return HTTP_OK;
    }

    String getPayload() {
      return captured.toString(StandardCharsets.UTF_8);
    }
  }
}
