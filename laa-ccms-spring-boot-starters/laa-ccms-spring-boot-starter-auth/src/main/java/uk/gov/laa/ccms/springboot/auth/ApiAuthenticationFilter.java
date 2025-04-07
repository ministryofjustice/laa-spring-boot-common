package uk.gov.laa.ccms.springboot.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * API access token authentication filter.
 */
@Slf4j
public class ApiAuthenticationFilter extends OncePerRequestFilter {

  private final AuthenticationManager authenticationManager;

  private final ObjectMapper objectMapper;

  private final TokenDetailsManager tokenDetailsManager;

  protected ApiAuthenticationFilter(
      AuthenticationManager authenticationManager, ObjectMapper objectMapper,
      TokenDetailsManager tokenDetailsManager) {
    this.authenticationManager = authenticationManager;
    this.objectMapper = objectMapper;
    this.tokenDetailsManager = tokenDetailsManager;
  }

  /**
   * Filter responsible for authenticating the client which made the request. Successful
   * authentication results in the authentication details being stored in the security context for
   * further processing, and continuation of the filter chain. Unsuccessful authentication results
   * in a 401 UNAUTHORIZED response.
   *
   * @param request     the http request object
   * @param response    the http response object
   * @param filterChain the current filter chain
   * @throws IOException      -
   * @throws ServletException -
   */
  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {

    try {
      String accessToken = request.getHeader(tokenDetailsManager.getAuthenticationHeader());
      ApiAuthenticationToken authRequest = ApiAuthenticationToken.unauthenticated(accessToken);
      Authentication authentication = authenticationManager.authenticate(authRequest);
      SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
      securityContext.setAuthentication(authentication);
      SecurityContextHolder.setContext(securityContext);
      log.info("Endpoint '{} {}' requested by {}.", request.getMethod(), request.getRequestURI(),
          authentication.getPrincipal().toString());

      filterChain.doFilter(request, response);

    } catch (AuthenticationException ex) {
      int code = HttpServletResponse.SC_UNAUTHORIZED;
      response.setStatus(code);
      response.setContentType(MediaType.APPLICATION_JSON_VALUE);

      String status = Response.Status.UNAUTHORIZED.getReasonPhrase();
      String message = ex.getMessage();

      ErrorResponse errorResponse = new ErrorResponse(code, status, message);
      response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
      log.info("Request rejected for endpoint '{} {}': {}", request.getMethod(),
          request.getRequestURI(), message);
    }

  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    // Skip if URI is in tokenDetailsManager.getUnprotectedUris()
    return Arrays.stream(tokenDetailsManager.getUnprotectedUris())
        .anyMatch(uri -> new AntPathRequestMatcher(uri).matches(request));
  }
}
