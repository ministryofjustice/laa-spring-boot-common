package uk.gov.laa.springboot.oauth2;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/** Properties for configuring trusted JWT tenants. */
@Getter
@Setter
public class MultiTenantJwtProperties {

  private List<Tenant> tenants = new ArrayList<>();

  /** A trusted JWT issuer and its accepted audiences. */
  @Getter
  @Setter
  public static class Tenant {
    private String issuerUri;
    private List<String> audiences = new ArrayList<>();
  }
}
