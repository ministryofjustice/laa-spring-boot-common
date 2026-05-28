package uk.gov.laa.springboot.cookies;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * Auto-configuration for Cookie starter.
 */
@AutoConfiguration
@ConditionalOnWebApplication
@Import({CookieConsentController.class, CookieBannerModelAdvice.class})
@EnableConfigurationProperties(CookieConsentProperties.class)
@ConditionalOnProperty(
        prefix = "laa.springboot.starter.cookie-consent",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class CookieConsentAutoConfiguration {
  private final CookieConsentProperties properties;

  public CookieConsentAutoConfiguration(CookieConsentProperties properties) {
    this.properties = properties;
  }
}
