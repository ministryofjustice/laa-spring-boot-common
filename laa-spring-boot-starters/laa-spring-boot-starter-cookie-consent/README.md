# LAA Spring Boot Starter - Cookie Consent

Provides a GOV.UK-style cookie starter for Spring Boot applications that use Thymeleaf.

The starter auto-configures:
- A cookie consent banner
- A cookie preferences page
- Analytics consent storage
- Cookie preference management
- Configurable analytics cookie information

## Usage

### 1. Declare dependency
```groovy
dependencies {
    implementation "uk.gov.laa.springboot:laa-spring-boot-starter-cookie-consent"
}
```

### 2. Add cookie banner
Consuming applications need to add the cookie banner to the main layout template:

```html
<div th:unless="${isCookiesPage}">
    <div th:replace="~{fragments/cookie-banner :: cookieBanner}"/>
</div>
```

### 3. Add a footer link
Applications should provide a link to the cookie preferences page:

```html
<a class="govuk-footer__link " href="/cookies">
    Cookies
</a>
```

### 4. Configure Analytics cookies
Applications can define the analytics cookies that are displayed in on the cookie preferences page.
For example, an application using Google Analytics can configure its analytics cookies as follows


```yaml
laa:
  springboot:
    starter:
      cookie-consent:
        default-redirect-path: /start
        cookies-policy: myapp_cookies_preferences
        banner-hidden-cookies: myapp_banner_hidden
        analytics-cookies:
        -  name: _ga
           purpose: Used to distinguish users
           expires: 2 years
```
The configured cookies will be displayed in the "Analytics cookies we use" table on the cookie preferences page,
allowing users to understand what analytics cookies are used and their purpose.

### 5. Configure analytics behaviour
The starter stores user consent in the `cookies_policy` cookie.
Applications can use this consent value to determine whether analytics should be loaded.

## Behaviour
- Displays a cookie banner until the user makes a choice.
- Stores cookie preferences in the `cookies_policy` cookie.
- Displays a confirmation banner after preferences are saved.
- Stores banner hidden decision in the `cookies_banner_hidden` cookie.
- Allows users to update their preferences at any time through the `/cookies` page.
- Resets the confirmation banner when cookie preferences are changed.
- Supports application-specific analytics cookie definitions through configuration.

## Configuration
| Property                                   | Description                                                                         | Default                 |
|--------------------------------------------|-------------------------------------------------------------------------------------|-------------------------|
| `laa.cookie-consent.enabled`               | Enabled cookie consent functionality                                              | `/cookies`              |
| `laa.cookie-consent.default-redirect-path` | Fallback redirect when no Referer header exists                                     | `/`                     |
| `laa.cookie-consent.cookies-policy`        | Name of the cookie used to store the user's analytics consent preferences           | `cookies_policy`        |
| `laa.cookie-consent.banner-hidden-cookie`  | Name of the cookie used to store whether the confirmation banner has been dismissed | `cookies_banner_hidden` |
| `laa.cookie-consent.analytics-cookies`     | Analytics cookies displayed on preference page                                      | `[]`                    |

## Endpoints
By default the starter exposes the following endpoints under 
the configured `laa.cookie-consent.cookies-path` (default: `/cookies`):

| Endpoint               | Description                        |
|------------------------|------------------------------------|
| `/cookies`             | Cookie preference page             |
| `/cookies/consent`     | Accept or reject analytics cookies |
| `/cookies/preferences` | Update cookie preferences          |
| `/cookies/hide`        | Hide confirmation banner           |

## Testing
The starter ships with unit and integration test covering:
- Auto-configuration
- Cookie consent controller endpoints
- Cookie consent interceptor behaviour
- Cookie preference persistence
- Cookie consent integration flows

No additional setup is required when consuming the starter.

## Future add-ons to this starter
This starter currently is developed only for essential and analytics cookies.
In the scenario that a new cookie type needs to be added to the consumer application,
the cookies needs to be versioned. This is because if a new cookies type
(eg. marketing cookies) is added to an application, the user needs to be informed, so the cookie
banner would need to re-appear.

## Spring security
The cookie starter does not configure Spring Security.
Cookie consent endpoints should be accessible without authentication so that users can review and update
their preferences at all times.
Applications using Spring Security should allow anonymous access to the cookie endpoints.
For example, using the default path:

``` java
.requestMatchers("/cookies/**").permitAll()
```

This ensures users can view and update cookie preferences before authentication.