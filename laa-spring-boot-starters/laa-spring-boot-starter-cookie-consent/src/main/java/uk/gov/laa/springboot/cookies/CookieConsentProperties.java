package uk.gov.laa.springboot.cookies;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Controller for Cookie consent.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "laa.cookie-consent")
public class CookieConsentProperties {
  private boolean enabled = true;
  private String cookieName = "cookies_policy";
  private int maxAge = 365;
  private boolean secure = true;

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public String getCookieName() {
    return cookieName;
  }

  public void setCookieName(String cookieName) {
    this.cookieName = cookieName;
  }

  public int getMaxAge() {
    return maxAge;
  }

  public void setMaxAge(int maxAge) {
    this.maxAge = maxAge;
  }

  public boolean isSecure() {
    return secure;
  }

  public void setSecure(boolean enabled) {
    this.secure = secure;
  }
}
