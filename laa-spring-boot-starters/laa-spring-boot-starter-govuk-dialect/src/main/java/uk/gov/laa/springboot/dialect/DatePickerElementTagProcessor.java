package uk.gov.laa.springboot.dialect;

import static org.springframework.util.StringUtils.hasText;

import java.util.Map;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IModel;
import org.thymeleaf.model.IModelFactory;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.AbstractElementTagProcessor;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.templatemode.TemplateMode;

/**
 * Transforms <moj:datepicker/> elements into standard HTML button elements.
 */
public class DatePickerElementTagProcessor extends AbstractElementTagProcessor {

  private static final String TAG_NAME = "datepicker";
  private static final int PRECEDENCE = 900;

  public DatePickerElementTagProcessor() {
    super(TemplateMode.HTML, "moj", TAG_NAME, true, null, false, PRECEDENCE);
  }

  @Override
  protected void doProcess(ITemplateContext context, IProcessableElementTag tag,
                           IElementTagStructureHandler structureHandler) {

    Map<String, String> attributes = ProcessorUtils.parseAttributes(context, tag);
    DatePickerAttributes datePickerAttributes = new DatePickerAttributes(
        attributes.getOrDefault("id", "date"),
        attributes.getOrDefault("name", "date"),
        attributes.getOrDefault("label", "Date"),
        attributes.get("hint"),
        attributes.get("errorMessage"),
        attributes.get("value"),
        attributes.get("minDate"),
        attributes.get("maxDate")
    );

    String datePickerHtml = buildDatePickerHtml(datePickerAttributes);
    final IModelFactory modelFactory = context.getModelFactory();
    final IModel model = modelFactory.parse(context.getTemplateData(), datePickerHtml);
    structureHandler.replaceWith(model, false);

  }

  private String buildDatePickerHtml(DatePickerAttributes datePickerAttributes) {
    StringBuilder html = new StringBuilder();
    html.append("<div class=\"moj-datepicker\" data-module=\"moj-date-picker\"");

    if (hasText(datePickerAttributes.minDate())) {
      html.append(" data-min-date=\"").append(datePickerAttributes.minDate()).append("\"");
    }
    if (hasText(datePickerAttributes.maxDate())) {
      html.append(" data-max-date=\"").append(datePickerAttributes.maxDate()).append("\"");
    }

    html.append(">")
        .append("<div class=\"govuk-form-group");

    if (datePickerAttributes.hasError()) {
      html.append(" govuk-form-group--error");
    }

    html.append("\">")
        .append("<label class=\"govuk-label\" for=\"").append(datePickerAttributes.id())
        .append("\">")
        .append(datePickerAttributes.label())
        .append("</label>")
        .append("<div id=\"").append(datePickerAttributes.id())
        .append("-hint\" class=\"govuk-hint\">")
        .append(datePickerAttributes.hint())
        .append("</div>");

    if (datePickerAttributes.hasError()) {
      html.append("<p id=\"").append(datePickerAttributes.id())
          .append("-error\" class=\"govuk-error-message\">")
          .append("<span class=\"govuk-visually-hidden\">Error:</span> ")
          .append(datePickerAttributes.errorMessage())
          .append("</p>");
    }

    html.append("<input class=\"govuk-input moj-js-datepicker-input");

    if (datePickerAttributes.hasError()) {
      html.append(" govuk-input--error");
    }

    html.append("\" id=\"").append(datePickerAttributes.id())
        .append("\" name=\"").append(datePickerAttributes.name())
        .append("\" type=\"text\" aria-describedby=\"").append(datePickerAttributes.id())
        .append("-hint");

    if (datePickerAttributes.hasError()) {
      html.append(" ").append(datePickerAttributes.id()).append("-error");
    }

    if (hasText(datePickerAttributes.value())) {
      html.append("\" value=\"").append(datePickerAttributes.value());
    }

    html.append("\" autocomplete=\"off\">")
        .append("</div>")
        .append("</div>");

    return html.toString();
  }
}
