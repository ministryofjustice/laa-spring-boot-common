package uk.gov.laa.springboot.cookies;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Auto-configuration for Cookie starter.
 */
@Configuration
@ConditionalOnWebApplication
//@EnableConfigurationProperties(CookieConsentProperties.class)
//@ConditionalOnProperty(
//        prefix = "laa.springboot.starter.cookie-consent",
//        name = "enabled",
//        havingValue = "true",
//        matchIfMissing = true
//)
public class CookieConsentAutoConfiguration implements WebMvcConfigurer {
  private final CookieConsentInterceptor cookieConsentInterceptor;

  public CookieConsentAutoConfiguration(CookieConsentInterceptor cookieConsentInterceptor) {
    this.cookieConsentInterceptor = cookieConsentInterceptor;
  }

  /**
   * Auto-configuration for Cookie starter.
   */
  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(cookieConsentInterceptor);
  }
}
