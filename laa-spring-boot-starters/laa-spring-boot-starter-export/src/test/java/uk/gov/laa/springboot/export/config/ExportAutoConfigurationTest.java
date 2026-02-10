package uk.gov.laa.springboot.export.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import uk.gov.laa.springboot.export.ExportAuditSink;
import uk.gov.laa.springboot.export.ExportRegistry;
import uk.gov.laa.springboot.export.ExportRequestValidator;
import uk.gov.laa.springboot.export.ExportService;
import uk.gov.laa.springboot.export.audit.LogExportAuditSink;
import uk.gov.laa.springboot.export.service.DefaultExportRequestValidator;
import uk.gov.laa.springboot.export.service.DefaultExportService;

class ExportAutoConfigurationTest {

  private final ApplicationContextRunner contextRunner =
      new ApplicationContextRunner()
          .withConfiguration(AutoConfigurations.of(ExportAutoConfiguration.class));

  @Test
  void createsExportBeansWhenEnabled() {
    contextRunner
        .withBean(
            "resourceProvider",
            uk.gov.laa.springboot.export.ExportCsvProvider.class,
            () -> (request, out, cols) -> 0L)
        .withBean(
            "libraryProvider",
            uk.gov.laa.springboot.export.ExportCsvProvider.class,
            () -> (request, out, cols) -> 0L)
        .withPropertyValues(
            "laa.springboot.starter.exports.enabled=true",
            "laa.springboot.starter.exports.definitions.sample.provider=libraryProvider",
            "laa.springboot.starter.exports.definitions.sample.params[0].name=status",
            "laa.springboot.starter.exports.definitions.sample.params[0].type=STRING")
        .run(
            context -> {
              assertThat(context).hasSingleBean(ExportService.class);
              assertThat(context).hasSingleBean(ExportRegistry.class);
              assertThat(context).hasSingleBean(ExportRequestValidator.class);
              assertThat(context).hasSingleBean(ExportAuditSink.class);

              assertThat(context.getBean(ExportService.class))
                  .isInstanceOf(DefaultExportService.class);
              assertThat(context.getBean(ExportRequestValidator.class))
                  .isInstanceOf(DefaultExportRequestValidator.class);
              assertThat(context.getBean(ExportAuditSink.class))
                  .isInstanceOf(LogExportAuditSink.class);
            });
  }

  @Test
  void doesNotCreateBeansWhenDisabled() {
    contextRunner
        .withPropertyValues("laa.springboot.starter.exports.enabled=false")
        .run(
            context -> {
              assertThat(context).doesNotHaveBean(ExportService.class);
              assertThat(context).doesNotHaveBean(ExportRegistry.class);
              assertThat(context).doesNotHaveBean(ExportRequestValidator.class);
              assertThat(context).doesNotHaveBean(ExportAuditSink.class);
            });
  }
}
