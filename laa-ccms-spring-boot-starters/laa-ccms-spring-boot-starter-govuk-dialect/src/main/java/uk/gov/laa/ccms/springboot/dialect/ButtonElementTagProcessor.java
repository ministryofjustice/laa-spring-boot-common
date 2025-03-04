package uk.gov.laa.ccms.springboot.dialect;

import java.util.Map;
import java.util.regex.Pattern;
import org.apache.logging.log4j.util.Strings;
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
public class ButtonElementTagProcessor extends AbstractElementTagProcessor {

  private static final String TAG_NAME = "button";
  private static final int PRECEDENCE = 900;
  private static final Pattern CLEANER = Pattern.compile("(?m)^[ \t]*\r?\n");

  public ButtonElementTagProcessor() {
    super(TemplateMode.HTML, "govuk", TAG_NAME, true, null, false, PRECEDENCE);
  }

  @Override
  protected void doProcess(ITemplateContext context, IProcessableElementTag tag,
                           IElementTagStructureHandler structureHandler) {

    Map<String, String> attributes = ProcessorUtils.parseAttributes(context, tag);
    String classNames = buildClassNames(attributes);
    String commonAttributes = buildCommonAttributes(classNames, attributes);
    String buttonAttributes = buildButtonAttributes(attributes);

    String html = attributes.containsKey("href") ? buildAnchorHtml(attributes, commonAttributes) :
        buildButtonHtml(attributes, commonAttributes, buttonAttributes);

    replaceElementWithHtml(context, structureHandler, html);
  }


  private String buildClassNames(Map<String, String> attributes) {
    String classNames = "govuk-button";
    if (attributes.containsKey("classes")) {
      classNames += " " + attributes.get("classes");
    }
    return classNames;
  }

  private String buildCommonAttributes(String classNames, Map<String, String> attributes) {
    StringBuilder commonAttributes = new StringBuilder();
    commonAttributes.append("class=\"").append(classNames)
        .append("\" data-module=\"govuk-button\"");
    if (attributes.containsKey("id")) {
      commonAttributes.append(" id=\"").append(attributes.get("id")).append("\"");
    }
    return commonAttributes.toString();
  }

  private String buildButtonAttributes(Map<String, String> attributes) {
    StringBuilder buttonAttributes = new StringBuilder();
    if (attributes.containsKey("name")) {
      buttonAttributes.append(" name=\"").append(attributes.get("name")).append("\"");
    }
    if (attributes.containsKey("disabled")) {
      buttonAttributes.append(" disabled aria-disabled=\"true\"");
    }
    if (attributes.containsKey("preventDoubleClick")) {
      buttonAttributes.append(" data-prevent-double-click=\"")
          .append(attributes.get("preventDoubleClick")).append("\"");
    }
    if (attributes.containsKey("value")) {
      buttonAttributes.append(" value=\"").append(attributes.get("value")).append("\"");
    }
    return buttonAttributes.toString();
  }

  private String buildAnchorHtml(Map<String, String> attributes, String commonAttributes) {
    return "<a href=\"" + attributes.getOrDefault("href", "#")
        + "\" role=\"button\" draggable=\"false\" " + commonAttributes + ">"
        + attributes.getOrDefault("text", "") + "</a>";
  }

  private String buildButtonHtml(Map<String, String> attributes, String commonAttributes,
                                 String buttonAttributes) {
    return "<button type=\"" + attributes.getOrDefault("type", "submit") + "\" "
        + buttonAttributes + " " + commonAttributes + ">" + attributes.getOrDefault("text", "")
        + "</button>";
  }

  private void replaceElementWithHtml(ITemplateContext context,
                                      IElementTagStructureHandler structureHandler, String html) {
    final String adjusted = CLEANER.matcher(html).replaceAll(Strings.EMPTY);
    final IModelFactory modelFactory = context.getModelFactory();
    final IModel model = modelFactory.parse(context.getTemplateData(), adjusted);
    structureHandler.replaceWith(model, false);
  }
}

