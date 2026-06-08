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
 * Controller responsible for managing cookie consent preferences
 * and rendering the cookie preferences page.
 */
@Controller
public class CookieConsentController {
  private final CookieConsentProperties properties;

  public CookieConsentController(CookieConsentProperties properties) {
    this.properties = properties;
  }

  /**
   * Saves cookie consent preferences submitted from the cookie banner
   * and redirects the user back to the originating page.
   *
   * @param analytics indicates whether the user accepted analytics cookies
   * @param response the current HTTP response
   * @param request the current HTTP request
   * @return redirect to the originating page
   */
  @PostMapping("/cookies/consent")
  public String handleCookieConsent(
            @RequestParam("analytics") String analytics,
            HttpServletResponse response, HttpServletRequest request) {

    String cookieValue = "{\"analytics\":" + "yes".equals(analytics) + "}";
    Cookie consentCookie = new Cookie(properties.getCookiesPolicy(),
            URLEncoder.encode(cookieValue, StandardCharsets.UTF_8));

    consentCookie.setPath("/");
    consentCookie.setHttpOnly(false);
    consentCookie.setSecure(true);
    // 1 year
    consentCookie.setMaxAge(365 * 24 * 60 * 60);

    String cookieHeader = String.format(
        "%s=%s; Max-Age=%d; Path=/; Secure; SameSite=Lax",
        consentCookie.getName(),
        consentCookie.getValue(),
        consentCookie.getMaxAge()
    );
    response.setHeader("Set-Cookie", cookieHeader);

    // Whenever cookies_policy expires or is reset through preferences or clearing cookies,
    // the cookies_banner_hidden also resets
    Cookie hiddenCookie = new Cookie(properties.getBannerHiddenCookie(), "");
    hiddenCookie.setPath("/");
    hiddenCookie.setMaxAge(0);
    hiddenCookie.setHttpOnly(false);
    hiddenCookie.setSecure(true);
    response.addCookie(hiddenCookie);

    // Redirect back to where the user came from
    String referer = request.getHeader("Referer");
    return "redirect:" + (referer != null ? referer : properties.getDefaultRedirectPath());
  }

  /**
   * Hides the cookie banner for the current user by storing a preference cookie.
   *
   * @param request the current HTTP request
   * @param response the current HTTP response
   * @return redirect to the originating page
   */
  @PostMapping("/cookies/hide")
  public String hideCookieMessage(
            HttpServletResponse response, HttpServletRequest request) {
    Cookie hiddenCookie = new Cookie(properties.getBannerHiddenCookie(), "true");
    hiddenCookie.setPath("/");
    hiddenCookie.setHttpOnly(false);
    hiddenCookie.setSecure(true);
    // 1 year as default, but this would reset when cookies_policy cookie resets
    // in order to keep them in sync
    hiddenCookie.setMaxAge(365 * 24 * 60 * 60);

    response.addCookie(hiddenCookie);

    String referer = request.getHeader("Referer");
    return "redirect:" + (referer != null ? referer : properties.getDefaultRedirectPath());
  }

  /**
   * Displays the cookies preferences page.
   *
   * @param success indicates whether cookie preferences were
   *                successfully updated.
   * @param model the Spring MVC model
   * @param request the current HTTP request
   * @return the cookie preferences page view
   */
  @GetMapping("/cookies")
  public String cookiesPage(HttpServletRequest request, Model model,
                            @RequestParam(value = "success", required = false) Boolean success) {
    Boolean analyticsConsented = (Boolean) request.getAttribute("analyticsConsented");
    model.addAttribute("analyticsCookiesEnabled", analyticsConsented != null && analyticsConsented);
    model.addAttribute("showSuccessBanner", success != null && success);

    return "pages/cookies";
  }

  /**
   * Saves the user's cookie preferences and stores them in a browser cookie.
   *
   * @param analytics indicates whether the user accepted analytics cookies
   * @param response the current HTTP response
   * @return redirect to the originating page
   */
  @PostMapping("/cookies/preferences")
  public String saveCookiePreferences(
            @RequestParam("analytics") String analytics, HttpServletResponse response) {
    String cookieValue = "{\"analytics\":" + "yes".equals(analytics) + "}";
    Cookie consentCookie = new Cookie(properties.getCookiesPolicy(),
            URLEncoder.encode(cookieValue, StandardCharsets.UTF_8));

    consentCookie.setPath("/");
    consentCookie.setHttpOnly(false);
    consentCookie.setSecure(true);
    // 1 year
    consentCookie.setMaxAge(365 * 24 * 60 * 60);

    String cookieHeader = String.format(
        "%s=%s; Max-Age=%d; Path=/; Secure; SameSite=Lax",
        consentCookie.getName(),
        consentCookie.getValue(),
        consentCookie.getMaxAge()
    );
    response.setHeader("Set-Cookie", cookieHeader);

    // Whenever cookies_policy expires or is reset through preferences or clearing cookies,
    // the cookies_banner_hidden also resets
    Cookie hiddenCookie = new Cookie(properties.getBannerHiddenCookie(), "");
    hiddenCookie.setPath("/");
    hiddenCookie.setMaxAge(0);
    hiddenCookie.setHttpOnly(false);
    hiddenCookie.setSecure(true);
    response.addCookie(hiddenCookie);

    // Redirect back to where the user came from
    return "redirect:/cookies?success=true";
  }
}
