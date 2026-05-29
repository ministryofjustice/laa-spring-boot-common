package uk.gov.laa.springboot.cookies;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for Cookie consent.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "laa.cookie-consent")
public class CookieConsentProperties {
  private boolean enabled = true;
  private String cookieName = "cookies_policy";
  private int maxAge = 365;
  private boolean secure = true;
  private List<AnalyticsCookie> analyticsCookies = new ArrayList<>();
}
