package uk.gov.laa.springboot.cookies;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CookieConsentInterceptorTest {
  private CookieConsentInterceptor interceptor;
  private CookieConsentProperties properties;
  @BeforeEach
  void setUp(){
    properties = new CookieConsentProperties();
    interceptor = new CookieConsentInterceptor(properties);
  }

  @Test
  void returnsTrueForNormalRequests(){
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    Object handler = new Object();
    when(request.getRequestURI()).thenReturn("/test");
    Boolean result = interceptor.preHandle(request, response, handler);
    assertThat(result).isTrue();
  }

  @Test
  void handlesMissingCookiesGracefully() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    Object handler = new Object();
    when(request.getRequestURI()).thenReturn("/home");
    when(request.getCookies()).thenReturn(null);
    Boolean result = interceptor.preHandle(request, response, handler);
    assertThat(result).isTrue();
  }

  @Test
  void handlesCookiesPresent() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    Cookie consentCookie = new Cookie(properties.getCookiesPolicy(), "{\"analytics\":true}");
    Object handler = new Object();
    when(request.getRequestURI()).thenReturn("/home");
    when(request.getCookies()).thenReturn(new Cookie[] {consentCookie});
    Boolean result = interceptor.preHandle(request, response, handler);
    assertThat(result).isTrue();
  }

  @Test
  void handlesMultipleCookies() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    Cookie consentCookie = new Cookie(properties.getCookiesPolicy(), "{\"analytics\":true}");
    Cookie sessionCookie = new Cookie("SESSION", "123");
    Object handler = new Object();
    when(request.getRequestURI()).thenReturn("/home");
    when(request.getCookies()).thenReturn(new Cookie[] {consentCookie, sessionCookie});
    Boolean result = interceptor.preHandle(request, response, handler);
    assertThat(result).isTrue();
  }

  @Test
  void handlesMalformedCookieGracefully() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    Cookie malformedCookie = new Cookie(properties.getCookiesPolicy(), "not-json");
    Object handler = new Object();
    when(request.getRequestURI()).thenReturn("/home");
    when(request.getCookies()).thenReturn(new Cookie[] {malformedCookie});
    Boolean result = interceptor.preHandle(request, response, handler);
    assertThat(result).isTrue();
  }

  @Test
  void handlesCookiesPageRequest() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    Object handler = new Object();
    when(request.getRequestURI()).thenReturn("/cookies");
    Boolean result = interceptor.preHandle(request, response, handler);
    assertThat(result).isTrue();
  }
}
