package uk.gov.laa.springboot.dialect;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Controller;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;

@SpringBootTest(classes = ThymeleafTestConfig.class)
class SelectElementTagProcessorFieldTest {

  @Autowired
  private SpringTemplateEngine templateEngine;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    ThymeleafViewResolver viewResolver = new ThymeleafViewResolver();
    viewResolver.setTemplateEngine(templateEngine);
    mockMvc = MockMvcBuilders.standaloneSetup(new FormController())
        .setViewResolvers(viewResolver)
        .build();
  }

  @Test
  void shouldBindValueNameAndIdFromTheFormObjectAndRenderLabelAsHeading() throws Exception {
    String renderedHtml = render();

    assertThat(renderedHtml).contains(
        "<div class=\"govuk-form-group\"><h2 class=\"govuk-label-wrapper\">"
            + "<label class=\"govuk-label govuk-label--m\" for=\"officeId\">Office</label></h2>"
            + "<select class=\"govuk-select\" id=\"officeId\" name=\"officeId\" autocomplete=\"off\" "
            + "data-module=\"accessible-autocomplete\" data-show-all-values=\"false\">"
            + "<option value=\"\">Please select</option>"
            + "<option value=\"11\">Bristol</option>"
            + "<option value=\"12\" selected>Cardiff</option>"
            + "</select></div>");
  }

  @Test
  void shouldBindNestedPathsAndShowFieldErrors() throws Exception {
    String renderedHtml = render();

    assertThat(renderedHtml).contains(
        "<div class=\"govuk-form-group govuk-form-group--error\">"
            + "<label class=\"govuk-label\" for=\"address.country\">Country</label>"
            + "<p id=\"address.country-error\" class=\"govuk-error-message\">"
            + "<span class=\"govuk-visually-hidden\">Error:</span> Select a country</p>"
            + "<select class=\"govuk-select govuk-select--error\" id=\"address.country\" "
            + "name=\"address.country\" autocomplete=\"off\" aria-describedby=\"address.country-error\" "
            + "data-module=\"accessible-autocomplete\" data-show-all-values=\"false\">"
            + "<option value=\"\" selected>Please select</option>"
            + "<option value=\"GBR\">United Kingdom</option>"
            + "</select></div>");
  }

  @Test
  void shouldLetExplicitAttributesOverrideTheBinding() throws Exception {
    String renderedHtml = render();

    assertThat(renderedHtml)
        .contains("<p id=\"rel-error\" class=\"govuk-error-message\">"
            + "<span class=\"govuk-visually-hidden\">Error:</span> Overridden message</p>")
        .contains("id=\"rel\" name=\"relationship\" autocomplete=\"off\"")
        .contains("<option value=\"SIB\" selected>SIB</option>");
  }

  private String render() throws Exception {
    return mockMvc.perform(get("/form")).andReturn().getResponse().getContentAsString();
  }

  @Controller
  static class FormController {

    @GetMapping("/form")
    String form(Model model) {
      Form form = new Form();
      form.setOfficeId(12);
      form.setRelationship("SIB");

      BindingResult bindingResult = new BeanPropertyBindingResult(form, "form");
      bindingResult.rejectValue("address.country", "required", "Select a country");
      bindingResult.rejectValue("relationship", "invalid", "Bound message");

      Map<String, String> countries = new LinkedHashMap<>();
      countries.put("GBR", "United Kingdom");

      model.addAttribute("form", form);
      model.addAttribute(BindingResult.MODEL_KEY_PREFIX + "form", bindingResult);
      model.addAttribute("offices", List.of(new Office(11, "Bristol"), new Office(12, "Cardiff")));
      model.addAttribute("countries", countries);
      model.addAttribute("relationships", List.of("PAR", "SIB"));
      return "test-select-field";
    }
  }

  public static class Form {

    private Integer officeId;
    private String relationship;
    private final Address address = new Address();

    public Integer getOfficeId() {
      return officeId;
    }

    public void setOfficeId(Integer officeId) {
      this.officeId = officeId;
    }

    public String getRelationship() {
      return relationship;
    }

    public void setRelationship(String relationship) {
      this.relationship = relationship;
    }

    public Address getAddress() {
      return address;
    }
  }

  public static class Address {

    private String country;

    public String getCountry() {
      return country;
    }

    public void setCountry(String country) {
      this.country = country;
    }
  }

  public static class Office {

    private final Integer id;
    private final String name;

    Office(Integer id, String name) {
      this.id = id;
      this.name = name;
    }

    public Integer getId() {
      return id;
    }

    public String getName() {
      return name;
    }
  }
}
