package uk.gov.laa.springboot.oauth2;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.util.StringUtils;
import tools.jackson.databind.ObjectMapper;

/**
 * Writes a JSON response for authentication failures.
 */
@Slf4j
public class Oauth2AuthenticationEntryPoint implements AuthenticationEntryPoint {

  private final ObjectMapper objectMapper;

  public Oauth2AuthenticationEntryPoint(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public void commence(HttpServletRequest request, HttpServletResponse response,
                       AuthenticationException authException) throws IOException {
    int code = HttpServletResponse.SC_UNAUTHORIZED;
    response.setStatus(code);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);

    String message = resolveMessage(request, authException);
    ErrorResponse errorResponse = new ErrorResponse(code, "UNAUTHORIZED", message);
    response.getWriter().write(objectMapper.writeValueAsString(errorResponse));

    log.info(
        "Request rejected for endpoint '{} {}': {}",
        request.getMethod(),
        request.getRequestURI(),
        message);
  }

  private String resolveMessage(HttpServletRequest request, AuthenticationException authException) {
    String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (!StringUtils.hasText(authorizationHeader)) {
      return "No API access token provided.";
    }
    return authException.getMessage();
  }
}
