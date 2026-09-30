package uk.gov.laa.springboot.dialect;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.thymeleaf.context.Context;
import org.thymeleaf.exceptions.TemplateProcessingException;
import org.thymeleaf.spring6.SpringTemplateEngine;

@SpringBootTest(classes = ThymeleafTestConfig.class)
class SelectElementTagProcessorTest {

  @Autowired
  private SpringTemplateEngine templateEngine;

  private Context context;

  @BeforeEach
  void setUp() {
    Map<String, String> countries = new LinkedHashMap<>();
    countries.put("GBR", "United Kingdom");
    countries.put("IRL", "Ireland");

    context = new Context();
    context.setVariable("courts", List.of(
        new Court("C1", "Bristol Crown Court"),
        new Court("C2", "Cardiff Crown Court")));
    context.setVariable("selectedCourt", "C2");
    context.setVariable("countries", countries);
    context.setVariable("relationships", List.of("Parent", "Sibling"));
    context.setVariable("colours", List.of(
        Map.of("code", "R", "label", "Red"),
        Map.of("code", "B", "label", "Blue")));
    context.setVariable("unsafe", List.of("a&b", "<script>"));
  }

  @Test
  void shouldRenderBeanItemsWithPlaceholderSelectionAndShowAllValues() {
    String renderedHtml = templateEngine.process("test-select", context);

    assertThat(renderedHtml).contains(
        "<div class=\"govuk-form-group\"><label class=\"govuk-label\" for=\"court\">Court</label>"
            + "<select class=\"govuk-select\" id=\"court\" name=\"court\" "
            + "data-module=\"accessible-autocomplete\" data-show-all-values=\"true\">"
            + "<option value=\"\">Please select</option>"
            + "<option value=\"C1\">Bristol Crown Court</option>"
            + "<option value=\"C2\" selected>Cardiff Crown Court</option>"
            + "</select></div>");
  }

  @Test
  void shouldRenderMapItemsAsValueAndLabelWithExplicitName() {
    String renderedHtml = templateEngine.process("test-select", context);

    assertThat(renderedHtml).contains(
        "<select class=\"govuk-select\" id=\"country\" name=\"address.country\" "
            + "data-module=\"accessible-autocomplete\" data-show-all-values=\"false\">"
            + "<option value=\"GBR\">United Kingdom</option>"
            + "<option value=\"IRL\">Ireland</option>"
            + "</select>");
  }

  @Test
  void shouldRenderHintAndErrorAndDescribeTheSelectByThem() {
    String renderedHtml = templateEngine.process("test-select", context);

    assertThat(renderedHtml).contains(
        "<div class=\"govuk-form-group govuk-form-group--error\">"
            + "<label class=\"govuk-label\" for=\"relationship\">Relationship</label>"
            + "<div id=\"relationship-hint\" class=\"govuk-hint\">Relationship to the client</div>"
            + "<p id=\"relationship-error\" class=\"govuk-error-message\">"
            + "<span class=\"govuk-visually-hidden\">Error:</span> Select a relationship</p>"
            + "<select class=\"govuk-select govuk-select--error govuk-!-width-one-half\" "
            + "id=\"relationship\" name=\"relationship\" "
            + "aria-describedby=\"relationship-hint relationship-error\" "
            + "data-module=\"accessible-autocomplete\" data-show-all-values=\"true\">"
            + "<option value=\"Parent\">Parent</option>"
            + "<option value=\"Sibling\">Sibling</option>"
            + "</select></div>");
  }

  @Test
  void shouldPassThroughDataAttributesAndDisable() {
    String renderedHtml = templateEngine.process("test-select", context);

    assertThat(renderedHtml).contains(
        "<select class=\"govuk-select\" id=\"colour\" name=\"colour\" "
            + "data-module=\"accessible-autocomplete\" data-show-all-values=\"false\" "
            + "data-display-value-id=\"colourDisplayValue\" disabled>"
            + "<option value=\"R\">Red</option>"
            + "<option value=\"B\">Blue</option>"
            + "</select>");
  }

  @Test
  void shouldTreatExpressionsResolvingToNullAsUnset() {
    String renderedHtml = templateEngine.process("test-select", context);

    assertThat(renderedHtml).contains(
        "<label class=\"govuk-label\" for=\"nullFlags\">Null flags</label>"
            + "<select class=\"govuk-select\" id=\"nullFlags\" name=\"nullFlags\" "
            + "data-module=\"accessible-autocomplete\" data-show-all-values=\"false\">");
  }

  @Test
  void shouldEscapeLabelsAndOptions() {
    String renderedHtml = templateEngine.process("test-select", context);

    assertThat(renderedHtml)
        .contains("<label class=\"govuk-label\" for=\"unsafe\">Pick &lt;one&gt;</label>")
        .contains("<option value=\"a&amp;b\" selected>a&amp;b</option>")
        .contains("<option value=\"&lt;script&gt;\">&lt;script&gt;</option>")
        .doesNotContain("<script>");
  }

  @Test
  void shouldRequireAnId() {
    assertThatThrownBy(() -> templateEngine.process("test-select-missing-id", context))
        .rootCause()
        .isInstanceOf(TemplateProcessingException.class)
        .hasMessageContaining("govuk:select requires an id attribute");
  }

  @Test
  void shouldRejectItemsThatAreNotACollection() {
    context.setVariable("courts", "not a list");

    assertThatThrownBy(() -> templateEngine.process("test-select-invalid-items", context))
        .rootCause()
        .isInstanceOf(TemplateProcessingException.class)
        .hasMessageContaining("govuk:select items must be a collection, array or map");
  }

  @Test
  void shouldRejectALabelHeadingThatIsNotAHeading() {
    assertThatThrownBy(() -> templateEngine.process("test-select-bad-heading", context))
        .rootCause()
        .isInstanceOf(TemplateProcessingException.class)
        .hasMessageContaining("govuk:select labelHeading must be one of h1 to h6");
  }

  public static class Court {

    private final String code;
    private final String description;

    Court(String code, String description) {
      this.code = code;
      this.description = description;
    }

    public String getCode() {
      return code;
    }

    public String getDescription() {
      return description;
    }
  }
}
