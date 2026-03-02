package uk.gov.laa.springboot.observability;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import co.elastic.logging.AdditionalField;
import co.elastic.logging.logback.EcsEncoder;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Configuration for ECS structured logging.
 */
@AutoConfiguration
@EnableConfigurationProperties(ObservabilityProperties.class)
@ConditionalOnProperty(
        prefix = "laa.springboot.starter.observability",
        name = "enabled",
        havingValue = "true"
)
public class ObservabilityAutoConfiguration {

  public ObservabilityAutoConfiguration(ObservabilityProperties properties) {
    configureEcsLogging(properties);
  }

  private void configureEcsLogging(ObservabilityProperties properties) {

    AdditionalField processPid = new AdditionalField();
    processPid.setKey("process.pid");
    processPid.setValue(String.valueOf(ProcessHandle.current().pid()));

    LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

    // ECS Encoder
    EcsEncoder ecsEncoder = new EcsEncoder();
    ecsEncoder.setServiceName(properties.getServiceName());
    ecsEncoder.setServiceVersion(properties.getServiceVersion());
    ecsEncoder.setServiceEnvironment(properties.getEnvironment());
    ecsEncoder.addAdditionalField(processPid);
    ecsEncoder.setContext(context);
    ecsEncoder.start();

    // Console appender
    ConsoleAppender<ILoggingEvent> consoleAppender = new ConsoleAppender<>();
    consoleAppender.setContext(context);
    consoleAppender.setEncoder(ecsEncoder);
    consoleAppender.start();

    // Attach to root logger
    Logger rootLogger = context.getLogger(Logger.ROOT_LOGGER_NAME);
    rootLogger.detachAndStopAllAppenders();
    rootLogger.addAppender(consoleAppender);
  }
}