package uk.gov.laa.springboot.cookies;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
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
    boolean consentCookiesPresent = false;
    boolean confirmationBannerHidden = false;
    if (request.getCookies() != null) {
      for (Cookie cookie : request.getCookies()) {
        if ((properties.getCookiesPolicy()).equals(cookie.getName())) {
          String val = URLDecoder.decode(cookie.getValue(), StandardCharsets.UTF_8);
          analyticsConsented = val.contains("\"analytics\":true");
          consentCookiesPresent = true;
        }
        if ((properties.getBannerHiddenCookie()).equals(cookie.getName())) {
          confirmationBannerHidden = true;
        }
      }
    }
    request.setAttribute("analyticsConsented", analyticsConsented);
    request.setAttribute("showCookieBanner", !consentCookiesPresent);
    request.setAttribute("showConfirmationBanner", consentCookiesPresent
            && !confirmationBannerHidden);
    request.setAttribute("analyticsCookies", properties.getAnalyticsCookies());

    // To be used in the consumer application.
    request.setAttribute("isCookiesPage",
            request.getRequestURI().equals("/cookies"));
    return true;
  }
}
