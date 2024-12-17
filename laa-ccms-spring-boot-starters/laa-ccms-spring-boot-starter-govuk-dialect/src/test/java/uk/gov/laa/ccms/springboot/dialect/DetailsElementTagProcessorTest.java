package uk.gov.laa.ccms.springboot.dialect;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@SpringBootTest(classes = ThymeleafTestConfig.class)
class DetailsElementTagProcessorTest {

  @Autowired
  private SpringTemplateEngine templateEngine;

  @Test
  void shouldRenderGovukButton() {

    Context context = new Context();
    String renderedHtml = templateEngine.process("test-details", context);
    assertThat(renderedHtml)
        .contains(
            "<details class=\"govuk-details\"><summary class=\"govuk-details__summary\">" +
                "<span class=\"govuk-details__summary-text\">Help with nationality</span>" +
                "</summary><div class=\"govuk-details__text\">We need to know your nationality " +
                "so we can work out which elections you're entitled to vote in.</div></details>")
        .contains(
            "<details class=\"govuk-details govuk-!-margin-top-2\"><summary " +
                "class=\"govuk-details__summary\"><span class=\"govuk-details__summary-text\">" +
                "Help with nationality</span></summary><div class=\"govuk-details__text\">" +
                "We need to know your nationality so we can work out which elections you're " +
                "entitled to vote in.</div></details>");

  }

}