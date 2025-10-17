package uk.gov.laa.springboot.slack;

/**
 * Service wrapper that exposes a simple API for sending Slack alerts.
 */
public class AlertService {

  private final SlackNotifier slackNotifier;

  public AlertService(SlackNotifier slackNotifier) {
    this.slackNotifier = slackNotifier;
  }

  public void sendAlertNotification(String message) {
    slackNotifier.sendSlackNotification(message, null, SlackIcon.INFORMATION);
  }

  public void sendAlertNotification(String message, SlackIcon icon) {
    slackNotifier.sendSlackNotification(message, null, icon);
  }

  public void sendAlertNotification(String message, String extraInfo, SlackIcon icon) {
    slackNotifier.sendSlackNotification(message, extraInfo, icon);
  }
}
