package uk.gov.laa.springboot.cookies;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

public class CookieConsentAutoConfigurationTest {
    private final WebApplicationContextRunner contextRunner =
            new WebApplicationContextRunner()
                    .withConfiguration(AutoConfigurations.of(CookieConsentAutoConfiguration.class));

    @Test
    void createsBeansWhenEnabled() {
        contextRunner
                .withPropertyValues(
                        "laa.springboot.starter.cookie-consent.enabled=true")
                .run(
                        context -> {
                            assertThat(context).hasSingleBean(CookieConsentInterceptor.class);
                            assertThat(context).hasSingleBean(CookieConsentController.class);
                        });
    }

    @Test
    void doesNotCreateBeansWhenDisabled() {
        contextRunner
                .withPropertyValues("laa.springboot.starter.cookie-consent.enabled=false")
                .run(
                        context -> {
                            assertThat(context).doesNotHaveBean(CookieConsentInterceptor.class);
                            assertThat(context).doesNotHaveBean(CookieConsentController.class);
                        });
    }
}
