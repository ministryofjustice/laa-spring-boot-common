package uk.gov.laa.ccms.springboot.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

/** API access token authentication filter. */
@Slf4j
public class ApiAuthenticationFilter extends GenericFilterBean {

  ApiAuthenticationService authenticationService;

  ObjectMapper objectMapper;

  @Autowired
  protected ApiAuthenticationFilter(
      ApiAuthenticationService authenticationService, ObjectMapper objectMapper) {
    this.authenticationService = authenticationService;
    this.objectMapper = objectMapper;
  }

  /**
   * Filter responsible for authenticating the client which made the request. Successful
   * authentication results in the authentication details being stored in the security context for
   * further processing, and continuation of the filter chain. Unsuccessful authentication results
   * in a 401 UNAUTHORIZED response.
   *
   * @param request the http request object
   * @param response the http response object
   * @param filterChain the current filter chain
   * @throws IOException -
   * @throws ServletException -
   */
  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
      throws IOException, ServletException {
    try {
      Authentication authentication =
          authenticationService.getAuthentication((HttpServletRequest) request);
      SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
      securityContext.setAuthentication(authentication);
      SecurityContextHolder.setContext(securityContext);
      log.info(
          "Endpoint '{} {}' requested by {}.",
          ((HttpServletRequest) request).getMethod(),
          ((HttpServletRequest) request).getRequestURI(),
          authentication.getPrincipal().toString());
      filterChain.doFilter(request, response);
    } catch (AuthenticationException ex) {
      int code = HttpServletResponse.SC_UNAUTHORIZED;
      HttpServletResponse httpResponse = (HttpServletResponse) response;
      httpResponse.setStatus(code);
      httpResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);

      String status = Response.Status.UNAUTHORIZED.getReasonPhrase();
      String message = ex.getMessage();

      ErrorResponse errorResponse = new ErrorResponse(code, status, message);

      httpResponse.getWriter().write(objectMapper.writeValueAsString(errorResponse));

      log.info(
          "Request rejected for endpoint '{} {}': {}",
          ((HttpServletRequest) request).getMethod(),
          ((HttpServletRequest) request).getRequestURI(),
          message);
    }
  }
}
