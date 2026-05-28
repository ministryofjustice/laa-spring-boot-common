package uk.gov.laa.springboot.cookies;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Auto-configuration for Cookie starter.
 */
@AutoConfiguration
@ConditionalOnWebApplication
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

  @Bean
  @ConditionalOnMissingBean
  public CookieConsentInterceptor cookieConsentInterceptor() {
    return new CookieConsentInterceptor(properties);
  }

  @Bean
  @ConditionalOnMissingBean
  public CookieConsentController cookieConsentController() {
    return new CookieConsentController(properties);
  }

  /**
   * Auto-configuration for Cookie starter.
   */
  @Bean
  public WebMvcConfigurer myWebMvcConfigurer(CookieConsentInterceptor interceptor) {
    return new WebMvcConfigurer() {
      @Override
      public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(interceptor).addPathPatterns("/**");
      }
    };
  }
}
