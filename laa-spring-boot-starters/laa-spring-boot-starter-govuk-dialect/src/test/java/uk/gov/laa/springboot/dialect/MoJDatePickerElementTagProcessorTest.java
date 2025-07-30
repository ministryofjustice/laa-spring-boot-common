package uk.gov.laa.springboot.dialect;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@SpringBootTest(classes = ThymeleafTestConfig.class)
class MoJDatePickerElementTagProcessorTest {

  @Autowired
  private SpringTemplateEngine templateEngine;

  @Test
  void shouldRenderGovukButton() {

    Context context = new Context();
    String renderedHtml = templateEngine.process("test-datepicker", context);
    assertThat(renderedHtml)
        .contains(
            "<div class=\"moj-datepicker\" data-module=\"moj-date-picker\" data-min-date=\"2000-01-01\" " +
                "data-max-date=\"2025-12-31\"><div class=\"govuk-form-group govuk-form-group--error\">" +
                "<label class=\"govuk-label\" for=\"dob\">Date of Birth</label><div id=\"dob-hint\" " +
                "class=\"govuk-hint\">For example, 01/01/2000.</div><p id=\"dob-error\" " +
                "class=\"govuk-error-message\"><span class=\"govuk-visually-hidden\">Error:</span> " +
                "Please enter a valid date of birth.</p><input class=\"govuk-input moj-js-datepicker-input " +
                "govuk-input--error\" id=\"dob\" name=\"dateOfBirth\" type=\"text\" " +
                "aria-describedby=\"dob-hint dob-error\" value=\"2024-01-01\" autocomplete=\"off\">" +
                "</div></div>")
        .contains(
            "<div class=\"moj-datepicker\" data-module=\"moj-date-picker\" " +
                "data-min-date=\"2000-01-01\" data-max-date=\"2025-12-31\">" +
                "<div class=\"govuk-form-group\"><label class=\"govuk-label\" " +
                "for=\"dob\">Date of Birth</label><div id=\"dob-hint\" " +
                "class=\"govuk-hint\">For example, 01/01/2000.</div><input " +
                "class=\"govuk-input moj-js-datepicker-input\" id=\"dob\" name=\"dateOfBirth\" " +
                "type=\"text\" aria-describedby=\"dob-hint\" value=\"2024-01-01\" " +
                "autocomplete=\"off\"></div></div>");

  }

}