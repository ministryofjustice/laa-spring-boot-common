package uk.gov.laa.springboot.cookies;

import lombok.Getter;
import lombok.Setter;

/**
 * Represents an analytics cookie displayed on the cookie preference page.
 */
@Getter
@Setter
public class AnalyticsCookie {
  private String name;
  private String purpose;
  private String expires;
}
