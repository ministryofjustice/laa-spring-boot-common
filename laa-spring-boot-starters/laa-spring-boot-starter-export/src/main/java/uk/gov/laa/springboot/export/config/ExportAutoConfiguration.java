package uk.gov.laa.springboot.export.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import uk.gov.laa.springboot.export.ExportAuditSink;
import uk.gov.laa.springboot.export.ExportRegistry;
import uk.gov.laa.springboot.export.ExportRequestValidator;
import uk.gov.laa.springboot.export.ExportService;
import uk.gov.laa.springboot.export.audit.LogExportAuditSink;
import uk.gov.laa.springboot.export.registry.DefaultExportRegistry;
import uk.gov.laa.springboot.export.service.DefaultExportRequestValidator;
import uk.gov.laa.springboot.export.service.DefaultExportService;

/**
 * Auto-configuration for export components.
 */
@AutoConfiguration
@EnableConfigurationProperties(LaaExportsProperties.class)
@ConditionalOnProperty(
        prefix = "laa.springboot.starter.exports",
        name = "enabled",
        havingValue = "true")
public class ExportAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean
  public ExportRequestValidator exportRequestValidator() {
    return new DefaultExportRequestValidator();
  }

  @Bean
  @ConditionalOnMissingBean
  public ExportAuditSink exportAuditSink() {
    return new LogExportAuditSink();
  }

  @Bean
  @ConditionalOnMissingBean
  public ExportRegistry exportRegistry(
      ApplicationContext applicationContext, LaaExportsProperties properties) {
    return new DefaultExportRegistry(applicationContext, properties);
  }

  @Bean
  @ConditionalOnMissingBean
  public ExportService exportService(
      ExportRegistry registry,
      ExportRequestValidator validator,
      ExportAuditSink audit) {
    return new DefaultExportService(registry, validator, audit);
  }

  @Bean
  @ConditionalOnMissingBean
  public ExportExceptionHandler exportExceptionHandler() {
    return new ExportExceptionHandler();
  }

}
