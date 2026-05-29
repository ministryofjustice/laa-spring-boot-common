package uk.gov.laa.springboot.cookies;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnalyticsCookie {
    private String name;
    private String purpose;
    private String expires;
}
