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
 * Transforms <govuk:button/> elements into standard HTML button elements.
 */
public class DetailsElementTagProcessor extends AbstractElementTagProcessor {

  private static final String TAG_NAME = "details";
  private static final int PRECEDENCE = 900;

  public DetailsElementTagProcessor() {
    super(TemplateMode.HTML, "govuk", TAG_NAME, true, null, false, PRECEDENCE);
  }

  @Override
  protected void doProcess(ITemplateContext context, IProcessableElementTag tag,
                           IElementTagStructureHandler structureHandler) {

    // Parse attributes
    Map<String, String> attributes = ProcessorUtils.parseAttributes(context, tag);
    String summaryText = attributes.getOrDefault("summaryText", "");
    String text = attributes.getOrDefault("text", "");

    // Build the HTML structure
    String detailsHtml = buildDetailsHtml(summaryText, text);

    // Create the model and replace the tag
    final IModelFactory modelFactory = context.getModelFactory();
    final IModel model = modelFactory.parse(context.getTemplateData(), detailsHtml);
    structureHandler.replaceWith(model, false);
  }

  private String buildDetailsHtml(String summaryText, String text) {
    return new StringBuilder()
        .append("<details class=\"").append("govuk-details").append("\">")
        .append("<summary class=\"").append("govuk-details__summary").append("\">")
        .append("<span class=\"").append("govuk-details__summary-text").append("\">")
        .append(summaryText)
        .append("</span>")
        .append("</summary>")
        .append("<div class=\"").append("govuk-details__text").append("\">")
        .append(text)
        .append("</div>")
        .append("</details>")
        .toString();
  }
}
