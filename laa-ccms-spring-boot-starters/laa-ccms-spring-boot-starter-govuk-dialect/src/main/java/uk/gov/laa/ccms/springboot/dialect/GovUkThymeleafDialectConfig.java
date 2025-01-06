package uk.gov.laa.ccms.springboot.dialect;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.thymeleaf.spring6.SpringTemplateEngine;

/**
 * Custom ThymeleafDialectConfig.
 */
@AutoConfiguration
@ConditionalOnClass(SpringTemplateEngine.class)
public class GovUkThymeleafDialectConfig {
  /**
   * SpringTemplateEngine with custom dialect.
   */
  @Bean
  public GovUkDialect govUkDialect() {
    return new GovUkDialect();
  }

  @Bean
  public MojCustomDialect mojCustomDialect() {
    return new MojCustomDialect();
  }

}
