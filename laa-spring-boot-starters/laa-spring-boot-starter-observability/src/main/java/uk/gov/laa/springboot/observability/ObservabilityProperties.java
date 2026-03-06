package uk.gov.laa.springboot.observability;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for ECS structured logging.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "laa.springboot.starter.observability")
public class ObservabilityProperties {

  /**
   * ECS logging configurable via property.
   */
  @NotNull(message = "enabled is required")
  private Boolean enabled;

  /**
   * The name of the service.
   */
  @NotBlank(message = "serviceName is required")
  private String serviceName;

  /**
   * The version of the service.
   */
  @NotBlank(message = "serviceVersion is required")
  private String serviceVersion;

  /**
   * The environment of the service.
   */
  @NotBlank(message = "environment is required")
  private String environment;

}