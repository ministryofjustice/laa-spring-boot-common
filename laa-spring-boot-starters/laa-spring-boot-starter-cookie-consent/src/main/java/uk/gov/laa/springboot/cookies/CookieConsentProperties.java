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
@ConfigurationProperties(prefix = "laa.springboot.starter.cookie-consent")
public class CookieConsentProperties {
  private boolean enabled = true;
  private String cookiesPolicy = "cookies_policy";
  private String bannerHiddenCookie = "cookies_banner_hidden";
  private List<AnalyticsCookie> analyticsCookies = new ArrayList<>();
  private String defaultRedirectPath = "/";
}
