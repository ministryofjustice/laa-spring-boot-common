package uk.gov.laa.springboot.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

/**
 * Exception Handler for requests that have been authenticated, but do not have sufficient
 * privileges to access the requested endpoint.
 */
@Slf4j
public class ApiAccessDeniedHandler implements AccessDeniedHandler {

  private final ObjectMapper objectMapper;

  /**
   * Creates an instance of the handler, with an object mapper to write the request body.
   *
   * @param objectMapper for writing the request body.
   */

  protected ApiAccessDeniedHandler(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  /**
   * Constructs the response object to return to the client, with a 403 Forbidden status and
   * matching response body using the {@link ErrorResponse} model.
   *
   * @param request               that resulted in an <code>AccessDeniedException</code>
   * @param response              so that the client can be advised of the failure
   * @param accessDeniedException that caused the invocation
   * @throws IOException -
   */
  @Override
  public void handle(
      HttpServletRequest request,
      HttpServletResponse response,
      AccessDeniedException accessDeniedException)
      throws IOException {
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
