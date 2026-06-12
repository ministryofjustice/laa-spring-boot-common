package uk.gov.laa.springboot.oauth2;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import tools.jackson.databind.ObjectMapper;

/**
 * Writes a JSON response for authentication failures.
 */
@Slf4j
public class Oauth2AuthenticationEntryPoint implements AuthenticationEntryPoint {

  private static final String UNAUTHORIZED_MESSAGE = "Unauthorized";

  private final ObjectMapper objectMapper;

  public Oauth2AuthenticationEntryPoint(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public void commence(HttpServletRequest request, HttpServletResponse response,
                       AuthenticationException authException) throws IOException {
    HttpStatus status = HttpStatus.UNAUTHORIZED;
    response.setStatus(status.value());
    response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);

    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, UNAUTHORIZED_MESSAGE);
    problemDetail.setTitle(status.getReasonPhrase());
    response.getWriter().write(objectMapper.writeValueAsString(problemDetail));

    log.info(
        "Request rejected for endpoint '{} {}': {}",
        request.getMethod(),
        request.getRequestURI(),
        authException.getMessage());
  }
}
