package uk.gov.laa.springboot.sqlscanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration that logs a warning when Spring AOP is missing.
 */
@AutoConfiguration
@ConditionalOnMissingClass("org.aspectj.lang.JoinPoint")
public class SqlScanMissingAopWarningAutoConfiguration {

  private static final Logger log =
      LoggerFactory.getLogger(SqlScanMissingAopWarningAutoConfiguration.class);

  @Bean
  ApplicationRunner sqlScanAopMissingWarning() {
    return args -> log.warn("LAA SQL scanner auto-configuration skipped because Spring AOP is not"
        + " on the classpath. Add 'spring-boot-starter-aop' to enable SQL scanning.");
  }
}
