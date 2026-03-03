package uk.gov.laa.springboot.observability;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration properties for ECS structured logging.
 */
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "laa.springboot.starter.observability")
public class ObservabilityProperties {

  /**
   * Enable ECS logging.
   */
  private boolean enabled = true;

  /**
   * The name of the service.
   */
  @NotBlank(message = "service-name is required")
  private String serviceName;

  /**
   * The version of the service.
   */
  @NotBlank(message = "service-version is required")
  private String serviceVersion;

  /**
   * The environment of the service.
   */
  @NotBlank(message = "environment is required")
  private String environment;

}