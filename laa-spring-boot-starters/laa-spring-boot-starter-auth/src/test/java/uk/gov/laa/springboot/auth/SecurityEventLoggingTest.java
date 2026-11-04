package uk.gov.laa.springboot.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import jakarta.servlet.FilterChain;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import tools.jackson.databind.ObjectMapper;

class SecurityEventLoggingTest {

  private static final String TOKEN = "super-secret-api-token";

  private final AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
  private final TokenDetailsManager tokenDetailsManager = mock(TokenDetailsManager.class);
  private final ListAppender<ILoggingEvent> filterLog = new ListAppender<>();
  private final ListAppender<ILoggingEvent> handlerLog = new ListAppender<>();
  private final Logger filterLogger = (Logger) LoggerFactory.getLogger(ApiAuthenticationFilter.class);
  private final Logger handlerLogger = (Logger) LoggerFactory.getLogger(ApiAccessDeniedHandler.class);

  @BeforeEach
  void attach() {
    filterLog.start();
    handlerLog.start();
    filterLogger.addAppender(filterLog);
    handlerLogger.addAppender(handlerLog);
    when(tokenDetailsManager.getAuthenticationHeader()).thenReturn(HttpHeaders.AUTHORIZATION);
  }

  @AfterEach
  void detach() {
    filterLogger.detachAppender(filterLog);
    handlerLogger.detachAppender(handlerLog);
    SecurityContextHolder.clearContext();
  }

  @Test
  void successfulAuthenticationLogsTheClientNotTheToken() throws Exception {
    when(authenticationManager.authenticate(any()))
        .thenReturn(
            ApiAuthenticationToken.authenticated(
                "caab-service", TOKEN, AuthorityUtils.createAuthorityList("ROLE_ALL")));

    runFilter();

    ILoggingEvent event = filterLog.list.getFirst();
    assertThat(fields(event))
        .containsEntry("event_type", "AUTHENTICATION_SUCCESS")
        .containsEntry("outcome", "SUCCESS")
        .containsEntry("client_id", "caab-service")
        .containsEntry("http_method", "POST")
        .containsEntry("path", "/cases");
    assertThat(event.getLevel()).isEqualTo(Level.INFO);
    assertNoToken(event);
  }

  @Test
  void aMissingTokenIsLoggedAsAFailure() throws Exception {
    when(authenticationManager.authenticate(any()))
        .thenThrow(new MissingCredentialsException("No API access token provided."));

    MockHttpServletResponse response = runFilter();

    ILoggingEvent event = filterLog.list.getFirst();
    assertThat(response.getStatus()).isEqualTo(401);
    assertThat(fields(event))
        .containsEntry("event_type", "AUTHENTICATION_FAILURE")
        .containsEntry("outcome", "FAILURE")
        .containsEntry("event_detail", "MissingCredentialsException");
    assertThat(event.getLevel()).isEqualTo(Level.INFO);
  }

  @Test
  void anInvalidTokenIsLoggedWithoutTheTokenValue() throws Exception {
    when(authenticationManager.authenticate(any()))
        .thenThrow(new BadCredentialsException("Invalid API access token provided."));

    runFilter();

    ILoggingEvent event = filterLog.list.getFirst();
    assertThat(fields(event)).containsEntry("event_detail", "BadCredentialsException");
    assertNoToken(event);
  }

  @Test
  void aForbiddenRequestIsLoggedWithTheClientAndTheRequest() throws Exception {
    SecurityContextHolder.getContext()
        .setAuthentication(
            ApiAuthenticationToken.authenticated(
                "caab-service", TOKEN, AuthorityUtils.createAuthorityList("ROLE_READ")));
    var handler = new ApiAccessDeniedHandler(new ObjectMapper());
    var response = new MockHttpServletResponse();

    handler.handle(
        new MockHttpServletRequest("POST", "/cases"), response, new AccessDeniedException("no"));

    ILoggingEvent event = handlerLog.list.getFirst();
    assertThat(response.getStatus()).isEqualTo(403);
    assertThat(fields(event))
        .containsEntry("event_type", "AUTHORIZATION_DENIED")
        .containsEntry("outcome", "DENIED")
        .containsEntry("client_id", "caab-service")
        .containsEntry("event_detail", "POST /cases");
    assertThat(event.getLevel()).isEqualTo(Level.INFO);
    assertNoToken(event);
  }

  @Test
  void aForbiddenRequestWithNobodyAuthenticatedStillLogs() throws Exception {
    var handler = new ApiAccessDeniedHandler(new ObjectMapper());

    handler.handle(
        new MockHttpServletRequest("GET", "/cases"),
        new MockHttpServletResponse(),
        new AccessDeniedException("no"));

    assertThat(fields(handlerLog.list.getFirst()))
        .containsEntry("event_type", "AUTHORIZATION_DENIED")
        .doesNotContainKey("client_id");
  }

  private MockHttpServletResponse runFilter() throws Exception {
    var filter = new ApiAuthenticationFilter(authenticationManager, new ObjectMapper(), tokenDetailsManager);
    var request = new MockHttpServletRequest("POST", "/cases");
    request.addHeader(HttpHeaders.AUTHORIZATION, TOKEN);
    var response = new MockHttpServletResponse();
    filter.doFilterInternal(request, response, mock(FilterChain.class));
    return response;
  }

  private static Map<String, Object> fields(ILoggingEvent event) {
    Map<String, Object> fields = new HashMap<>();
    List.copyOf(event.getKeyValuePairs()).forEach(kv -> {
      if (kv.value != null) {
        fields.put(kv.key, kv.value);
      }
    });
    return fields;
  }

  private static void assertNoToken(ILoggingEvent event) {
    assertThat(event.getFormattedMessage()).doesNotContain(TOKEN);
    assertThat(fields(event).values().toString()).doesNotContain(TOKEN);
  }
}
