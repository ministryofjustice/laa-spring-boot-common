package uk.gov.laa.springboot.observability;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "laa.springboot.starter.observability")
public class ObservabilityProperties {

  /** ECS logging configurable via property */
  @NotBlank(message = "enabled is required")
  private Boolean enabled;

  /** The name of the service */
  @NotBlank(message = "serviceName is required")
  private String serviceName;

  /** The version of the service */
  @NotBlank(message = "serviceVersion is required")
  private String serviceVersion;

  /** The environment of the service */
  @NotBlank(message = "environment is required")
  private String environment;

}