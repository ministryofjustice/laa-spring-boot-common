package uk.gov.laa.springboot.cookies;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Model Advice for Cookie consent.
 */
@ControllerAdvice
public class CookieBannerModelAdvice {
  private final CookieConsentProperties properties;

  public CookieBannerModelAdvice(CookieConsentProperties properties) {
    this.properties = properties;
  }

  /**
   * Interceptor for Cookie consent.
   */
  @ModelAttribute
  public void addCookieBannerAttributes(HttpServletRequest request,
                                          Model model) {
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

    model.addAttribute("analyticsConsented", analyticsConsented);
    model.addAttribute("bannerSeen", bannerSeen && !bannerHidden);
    model.addAttribute("showCookieBanner", !bannerSeen);
    model.addAttribute("isCookiesPage", request.getRequestURI().equals("/cookies"));
  }
}
