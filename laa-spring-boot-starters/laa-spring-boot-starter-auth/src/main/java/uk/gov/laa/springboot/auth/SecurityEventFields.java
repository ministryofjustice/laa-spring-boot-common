package uk.gov.laa.springboot.auth;

/** Names of the structured log fields written for security events. */
final class SecurityEventFields {

  static final String EVENT_TYPE = "event_type";
  static final String OUTCOME = "outcome";
  static final String EVENT_DETAIL = "event_detail";
  static final String CLIENT_ID = "client_id";
  static final String HTTP_METHOD = "http_method";
  static final String PATH = "path";

  private SecurityEventFields() {}
}
