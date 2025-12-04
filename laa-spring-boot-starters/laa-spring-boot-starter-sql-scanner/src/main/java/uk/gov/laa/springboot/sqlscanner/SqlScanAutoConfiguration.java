package uk.gov.laa.springboot.sqlscanner;

import org.aspectj.lang.JoinPoint;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for SQL scanning of controller arguments.
 */
@AutoConfiguration
@ConditionalOnClass(JoinPoint.class)
public class SqlScanAutoConfiguration {

  /**
   * Provides the default scanner.
   *
   * @return the scanner.
   */
  @Bean
  @ConditionalOnMissingBean
  public SqlScanner sqlScanner() {
    return new SqlScanner();
  }

  /**
   * Provides the aspect that inspects controller arguments.
   *
   * @param sqlScanner the scanner to delegate detection to.
   * @return the aspect.
   */
  @Bean
  @ConditionalOnMissingBean
  public SqlScanAspect sqlScanAspect(SqlScanner sqlScanner) {
    return new SqlScanAspect(sqlScanner);
  }
}
