package uk.gov.laa.springboot.oauth2;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import tools.jackson.databind.ObjectMapper;

/**
 * Writes a JSON response for authorization failures.
 */
@Slf4j
public class Oauth2AccessDeniedHandler implements AccessDeniedHandler {

  private final ObjectMapper objectMapper;

  public Oauth2AccessDeniedHandler(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public void handle(HttpServletRequest request, HttpServletResponse response,
                     AccessDeniedException accessDeniedException) throws IOException {
    int code = HttpServletResponse.SC_FORBIDDEN;
    response.setStatus(code);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);

    String message = accessDeniedException.getMessage();
    ErrorResponse errorResponse = new ErrorResponse(code, "FORBIDDEN", message);
    response.getWriter().write(objectMapper.writeValueAsString(errorResponse));

    log.info(
        "Request rejected for endpoint '{} {}': {}",
        request.getMethod(),
        request.getRequestURI(),
        message);
  }
}
