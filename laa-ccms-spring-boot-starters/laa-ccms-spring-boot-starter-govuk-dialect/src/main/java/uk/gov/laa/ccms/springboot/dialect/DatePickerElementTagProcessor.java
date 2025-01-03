package uk.gov.laa.ccms.springboot.dialect;

import java.util.Map;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IModel;
import org.thymeleaf.model.IModelFactory;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.AbstractElementTagProcessor;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.templatemode.TemplateMode;

/**
 * hello.
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

    String id = attributes.getOrDefault("id", "date");
    String name = attributes.getOrDefault("name", "date");
    String value = attributes.get("value");
    String label = attributes.getOrDefault("label", "Date");
    String hint = attributes.getOrDefault("hint", "For example, 17/5/2024.");
    String errorMessage = attributes.getOrDefault("errorMessage", "Enter or select a date");
    String minDate = attributes.get("minDate");
    String maxDate = attributes.get("maxDate");

    boolean hasError = !errorMessage.isEmpty();

    String datePickerHtml =
        buildDatePickerHtml(id, name, label, hint, errorMessage, hasError, minDate, maxDate);

    final IModelFactory modelFactory = context.getModelFactory();
    final IModel model = modelFactory.parse(context.getTemplateData(), datePickerHtml);
    structureHandler.replaceWith(model, false);

  }

  private String buildDatePickerHtml(String id, String name, String label, String hint,
                                     String errorMessage, boolean hasError, String minDate,
                                     String maxDate) {
    StringBuilder html = new StringBuilder();
    html.append("<div class=\"moj-datepicker\" data-module=\"moj-date-picker\"");

    if (!minDate.isEmpty()) {
      html.append(" data-min-date=\"").append(minDate).append("\"");
    }
    if (!maxDate.isEmpty()) {
      html.append(" data-max-date=\"").append(maxDate).append("\"");
    }

    html.append(">")
        .append("<div class=\"govuk-form-group");

    if (hasError) {
      html.append(" govuk-form-group--error");
    }

    html.append("\">")
        .append("<label class=\"govuk-label\" for=\"").append(id).append("\">")
        .append(label)
        .append("</label>")
        .append("<div id=\"").append(id).append("-hint\" class=\"govuk-hint\">")
        .append(hint)
        .append("</div>");

    if (hasError) {
      html.append("<p id=\"").append(id).append("-error\" class=\"govuk-error-message\">")
          .append("<span class=\"govuk-visually-hidden\">Error:</span> ")
          .append(errorMessage)
          .append("</p>");
    }

    html.append("<input class=\"govuk-input moj-js-datepicker-input");

    if (hasError) {
      html.append(" govuk-input--error");
    }

    html.append("\" id=\"").append(id)
        .append("\" name=\"").append(name)
        .append("\" type=\"text\" aria-describedby=\"").append(id).append("-hint");

    if (hasError) {
      html.append(" ").append(id).append("-error");
    }

    html.append("\" autocomplete=\"off\">")
        .append("</div>")
        .append("</div>");

    return html.toString();
  }
}
