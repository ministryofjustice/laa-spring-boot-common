package uk.gov.laa.springboot.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AuthenticationManager;
import tools.jackson.databind.ObjectMapper;

class ApiAuthenticationFilterTest {

  private final TokenDetailsManager tokenDetailsManager = mock(TokenDetailsManager.class);

  private final ApiAuthenticationFilter filter = new ApiAuthenticationFilter(
      mock(AuthenticationManager.class), new ObjectMapper(), tokenDetailsManager);

  @Test
  void shouldNotFilterBearerAuthorizationHeader() {
    when(tokenDetailsManager.getAuthenticationHeader()).thenReturn(HttpHeaders.AUTHORIZATION);

    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/submissions");
    request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer jwt-token");

    assertThat(filter.shouldNotFilter(request)).isTrue();
  }

  @Test
  void shouldFilterApiKeyAuthorizationHeader() {
    when(tokenDetailsManager.getAuthenticationHeader()).thenReturn(HttpHeaders.AUTHORIZATION);
    when(tokenDetailsManager.getUnprotectedUris()).thenReturn(new String[0]);

    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/submissions");
    request.addHeader(HttpHeaders.AUTHORIZATION, "api-key-token");

    assertThat(filter.shouldNotFilter(request)).isFalse();
  }

  @Test
  void shouldNotFilterUnprotectedUris() {
    when(tokenDetailsManager.getAuthenticationHeader()).thenReturn(HttpHeaders.AUTHORIZATION);
    when(tokenDetailsManager.getUnprotectedUris()).thenReturn(new String[] {"/actuator/**"});

    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/actuator/health");

    assertThat(filter.shouldNotFilter(request)).isTrue();
  }
}
