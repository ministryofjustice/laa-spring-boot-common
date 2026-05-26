package uk.gov.laa.springboot.cookies;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller for Cookie consent.
 */
@Controller
public class CookieConsentController {
  private final CookieConsentProperties properties;

    public CookieConsentController(CookieConsentProperties properties) {
        this.properties = properties;
    }

    /**
   * Controller for Cookie consent.
   */
  @PostMapping("/cookies/consent")
  public String handleCookieConsent(
            @RequestParam("analytics") String analytics,
            HttpServletResponse response, HttpServletRequest request) {
    // Build the preference value \
    String cookieValue = "{\"analytics\":" + "yes".equals(analytics) + "}";
    Cookie consentCookie = new Cookie(properties.getCookieName(),
            URLEncoder.encode(cookieValue, StandardCharsets.UTF_8));

    consentCookie.setPath("/");
    consentCookie.setHttpOnly(false);
    consentCookie.setSecure(properties.isSecure());
    // 1 year
    consentCookie.setMaxAge(365 * 24 * 60 * 60);

    // Must be readable by JS for GA4 consentCookie.setSecure(true);
    response.addCookie(consentCookie);
    // Redirect back to where the user came from
    String referer = request.getHeader("Referer");
    return "redirect:" + (referer != null ? referer : "/");
  }

  /**
   * Controller for Cookie consent.
   */
  @PostMapping("/cookies/hide")
  public String hideCookieMessage(
            HttpServletResponse response, HttpServletRequest request) {
    Cookie hiddenCookie = new Cookie("cookies_banner_hidden", "true");
    hiddenCookie.setPath("/");
    hiddenCookie.setHttpOnly(false);
    hiddenCookie.setSecure(properties.isSecure());
    // 1 year
    hiddenCookie.setMaxAge(properties.getMaxAge() * 24 * 60 * 60);

    response.addCookie(hiddenCookie);

    String referer = request.getHeader("Referer");
    return "redirect:" + (referer != null ? referer : "/");
  }

  /**
   * Controller for Cookie consent.
   */
  @GetMapping("/cookies")
  public String cookiesPage(HttpServletRequest request, Model model,
                            @RequestParam(required = false) Boolean success) {
    Boolean analyticsConsented = (Boolean) request.getAttribute("analyticsConsented");
    model.addAttribute("analyticsCookiesEnabled", analyticsConsented != null && analyticsConsented);
    model.addAttribute("showSuccessBanner", success != null && success);

    return "pages/cookies";
  }

  /**
   * Controller for Cookie consent.
   */
  @PostMapping("/cookies/preferences")
  public String saveCookiePreferences(
            @RequestParam("analytics") String analytics, HttpServletResponse response) {
    String cookieValue = "{\"analytics\":" + "yes".equals(analytics) + "}";
    Cookie consentCookie = new Cookie(properties.getCookieName(),
            URLEncoder.encode(cookieValue, StandardCharsets.UTF_8));

    consentCookie.setPath("/");
    consentCookie.setHttpOnly(false);
    consentCookie.setSecure(properties.isSecure());
    // 1 year
    consentCookie.setMaxAge(properties.getMaxAge() * 24 * 60 * 60);

    // Must be readable by JS for GA4 consentCookie.setSecure(true);
    response.addCookie(consentCookie);
    // Redirect back to where the user came from
    return "redirect:/cookies?success=true";
  }
}
