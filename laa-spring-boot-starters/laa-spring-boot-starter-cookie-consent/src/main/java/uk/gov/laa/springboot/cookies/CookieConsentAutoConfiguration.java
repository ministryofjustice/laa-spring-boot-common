package uk.gov.laa.springboot.cookies;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Controller for Cookie consent.
 */
@Configuration
@RequiredArgsConstructor
@ConditionalOnWebApplication
@EnableConfigurationProperties(CookieConsentProperties.class)
@ConditionalOnProperty(
        prefix = "laa.cookie-consent",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class CookieConsentAutoConfiguration implements WebMvcConfigurer {
  private final CookieConsentInterceptor cookieConsentInterceptor;

  public void addInterceptor(InterceptorRegistry registry) {
    registry.addInterceptor(cookieConsentInterceptor);
  }
}
