package uk.gov.laa.springboot.cookies;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor for Cookie consent.
 */
public class CookieConsentInterceptor implements HandlerInterceptor {
  private final CookieConsentProperties properties;

  public CookieConsentInterceptor(CookieConsentProperties properties) {
    this.properties = properties;
  }

  @Override
  public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {
    boolean analyticsConsented = false;
    boolean bannerSeen = false;
    boolean bannerHidden = false;
    if (request.getCookies() != null) {
      for (Cookie cookie : request.getCookies()) {
        if (properties.getCookieName().equals(cookie.getName())) {
          String val = URLDecoder.decode(cookie.getValue(), StandardCharsets.UTF_8);
          analyticsConsented = val.contains("\"analytics\":true");
          bannerSeen = true;
        }
        if ("cookies_banner_hidden".equals(cookie.getName())) {
          bannerHidden = true;
        }
      }
    }
    request.setAttribute("analyticsConsented", analyticsConsented);
    request.setAttribute("showCookieBanner", !bannerSeen);
    request.setAttribute("showConfirmationBanner", bannerSeen && !bannerHidden);
    request.setAttribute("analyticsCookies", properties.getAnalyticsCookies());
    request.setAttribute("isCookiesPage", request.getRequestURI().equals("/cookies"));
    return true;
  }
}
