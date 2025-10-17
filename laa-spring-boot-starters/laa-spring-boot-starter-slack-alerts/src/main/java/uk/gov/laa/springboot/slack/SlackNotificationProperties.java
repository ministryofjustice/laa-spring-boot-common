package uk.gov.laa.springboot.slack;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

/**
 * Configuration properties for Slack notifications.
 */
@ConfigurationProperties(prefix = "laa.springboot.starter.slack-alerts")
public class SlackNotificationProperties {

  private boolean enabled = true;
  private String webhook;
  private String environmentName;
  private final Pod pod = new Pod();

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public String getWebhook() {
    return webhook;
  }

  public void setWebhook(String webhook) {
    this.webhook = webhook;
  }

  public String getEnvironmentName() {
    return environmentName;
  }

  public void setEnvironmentName(String environmentName) {
    this.environmentName = environmentName;
  }

  public Pod getPod() {
    return pod;
  }

  /**
   * Defines pod metadata that can be exported via the Kubernetes
   * Downward API and injected as environment variables.
   */
  public static class Pod {
    private String namespace;
    private String name;
    private String ip;

    public String getNamespace() {
      return namespace;
    }

    public void setNamespace(String namespace) {
      this.namespace = namespace;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getIp() {
      return ip;
    }

    public void setIp(String ip) {
      this.ip = ip;
    }
  }

  boolean hasWebhook() {
    return StringUtils.hasText(webhook);
  }
}
