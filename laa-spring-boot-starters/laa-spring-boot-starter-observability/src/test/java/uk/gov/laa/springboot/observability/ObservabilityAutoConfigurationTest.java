//package uk.gov.laa.springboot.observability;
//
//import ch.qos.logback.classic.Logger;
//import org.junit.jupiter.api.Test;
//import org.slf4j.LoggerFactory;
//import org.springframework.boot.test.context.runner.ApplicationContextRunner;
//
//import java.io.ByteArrayOutputStream;
//import java.io.PrintStream;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//class ObservabilityAutoConfigurationTest {
//
//  private final ApplicationContextRunner contextRunner =
//          new ApplicationContextRunner()
//                  .withUserConfiguration(ObservabilityAutoConfiguration.class)
//                  .withPropertyValues(
//                          "laa.springboot.starter.observability.enabled=true",
//                          "laa.springboot.starter.observability.service-name=test-service",
//                          "laa.springboot.starter.observability.service-version=1.0.0",
//                          "laa.springboot.starter.observability.environment=test"
//                  );
//
//  @Test
//  void createsBeansWhenEnabled() {
//    contextRunner
//            .withPropertyValues(
//                    "laa.springboot.starter.observability.enabled=true")
//            .run(
//                    context -> {
//                      assertThat(context).hasSingleBean(ObservabilityAutoConfiguration.class);
//                    });
//  }
//
//  @Test
//  void shouldNotLoadAutoConfigurationWhenDisabled() {
//    new ApplicationContextRunner()
//            .withUserConfiguration(ObservabilityAutoConfiguration.class)
//            .withPropertyValues(
//                    "laa.springboot.starter.observability.enabled=false"
//            )
//            .run(context ->
//                    assertThat(context)
//                            .doesNotHaveBean(ObservabilityAutoConfiguration.class)
//            );
//  }
//
//  @Test
//  void shouldCreateEcsWithConfiguredServiceFields() {
//
//    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//    System.setOut(new PrintStream(outputStream));
//
//    contextRunner.run(context -> {
//
//      Logger logger = (Logger) LoggerFactory.getLogger("test.logger");
//      logger.info("log message");
//      String logOutput = outputStream.toString();
//
//      assertThat(logOutput).contains("\"service.name\":\"test-service\"");
//      assertThat(logOutput).contains("\"service.version\":\"1.0.0\"");
//      assertThat(logOutput).contains("\"service.environment\":\"test\"");
//      assertThat(logOutput).contains("\"process.pid\"");
//      assertThat(logOutput).contains("\"log message\"");
//    });
//  }
//}