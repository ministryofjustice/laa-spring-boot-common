package uk.gov.laa.springboot.oauth2;

import static org.assertj.core.api.Assertions.assertThat;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import tools.jackson.databind.ObjectMapper;

class SecurityEventLoggingTest {

  private final ListAppender<ILoggingEvent> entryPointLog = new ListAppender<>();
  private final ListAppender<ILoggingEvent> handlerLog = new ListAppender<>();
  private final Logger entryPointLogger =
      (Logger) LoggerFactory.getLogger(Oauth2AuthenticationEntryPoint.class);
  private final Logger handlerLogger =
      (Logger) LoggerFactory.getLogger(Oauth2AccessDeniedHandler.class);

  @BeforeEach
  void attach() {
    entryPointLog.start();
    handlerLog.start();
    entryPointLogger.addAppender(entryPointLog);
    handlerLogger.addAppender(handlerLog);
  }

  @AfterEach
  void detach() {
    entryPointLogger.detachAppender(entryPointLog);
    handlerLogger.detachAppender(handlerLog);
  }

  @Test
  void anInvalidBearerTokenIsLoggedWithItsErrorCode() throws Exception {
    var response = new MockHttpServletResponse();

    new Oauth2AuthenticationEntryPoint(new ObjectMapper())
        .commence(
            new MockHttpServletRequest("GET", "/cases"),
            response,
            new InvalidBearerTokenException("Jwt expired at 2026-01-01T00:00:00Z"));

    ILoggingEvent event = entryPointLog.list.getFirst();
    assertThat(response.getStatus()).isEqualTo(401);
    assertThat(fields(event))
        .containsEntry("event_type", "AUTHENTICATION_FAILURE")
        .containsEntry("outcome", "FAILURE")
        .containsEntry("event_detail", "invalid_token")
        .containsEntry("http_method", "GET")
        .containsEntry("path", "/cases");
    assertThat(event.getLevel()).isEqualTo(Level.INFO);
  }

  @Test
  void otherFailuresAreLoggedByExceptionClass() throws Exception {
    new Oauth2AuthenticationEntryPoint(new ObjectMapper())
        .commence(
            new MockHttpServletRequest("GET", "/cases"),
            new MockHttpServletResponse(),
            new BadCredentialsException("no"));

    assertThat(fields(entryPointLog.list.getFirst()))
        .containsEntry("event_detail", "BadCredentialsException");
  }

  @Test
  void aForbiddenRequestIsLoggedWithoutAPrincipal() throws Exception {
    var response = new MockHttpServletResponse();

    new Oauth2AccessDeniedHandler(new ObjectMapper())
        .handle(
            new MockHttpServletRequest("POST", "/cases"),
            response,
            new AccessDeniedException("no"));

    ILoggingEvent event = handlerLog.list.getFirst();
    assertThat(response.getStatus()).isEqualTo(403);
    assertThat(fields(event))
        .containsEntry("event_type", "AUTHORIZATION_DENIED")
        .containsEntry("outcome", "DENIED")
        .containsEntry("event_detail", "POST /cases")
        .doesNotContainKeys("client_id", "user_id");
    assertThat(event.getLevel()).isEqualTo(Level.INFO);
  }

  private static Map<String, Object> fields(ILoggingEvent event) {
    Map<String, Object> fields = new HashMap<>();
    event.getKeyValuePairs().forEach(kv -> fields.put(kv.key, kv.value));
    return fields;
  }
}
