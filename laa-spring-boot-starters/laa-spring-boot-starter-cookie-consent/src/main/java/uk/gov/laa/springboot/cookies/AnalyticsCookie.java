package uk.gov.laa.springboot.cookies;

/**
 * Represents an analytics cookie displayed on the cookie preference page.
 */
public record AnalyticsCookie(
        String name,
        String purpose,
        String expires) {}
