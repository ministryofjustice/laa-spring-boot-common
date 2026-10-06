package uk.gov.laa.springboot.dialect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.springframework.beans.BeanWrapperImpl;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.exceptions.TemplateProcessingException;
import org.thymeleaf.model.IModel;
import org.thymeleaf.model.IModelFactory;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.AbstractElementTagProcessor;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.spring6.context.IThymeleafBindStatus;
import org.thymeleaf.spring6.util.FieldUtils;
import org.thymeleaf.templatemode.TemplateMode;
import org.unbescape.html.HtmlEscape;
import org.unbescape.html.HtmlEscapeLevel;
import org.unbescape.html.HtmlEscapeType;

/**
 * Renders &lt;govuk:select/&gt; as a GOV.UK select for the accessible autocomplete.
 */
public class SelectElementTagProcessor extends AbstractElementTagProcessor {

  private static final String DATA_MODULE = "accessible-autocomplete";

  private static final Set<String> HEADINGS = Set.of("h1", "h2", "h3", "h4", "h5", "h6");
  private static final Set<String> RESERVED_DATA_ATTRIBUTES =
      Set.of("data-module", "data-show-all-values");
  private static final String TAG_NAME = "select";
  private static final int PRECEDENCE = 900;

  public SelectElementTagProcessor() {
    super(TemplateMode.HTML, "govuk", TAG_NAME, true, null, false, PRECEDENCE);
  }

  @Override
  protected void doProcess(ITemplateContext context, IProcessableElementTag tag,
                           IElementTagStructureHandler structureHandler) {

    Map<String, Object> attributes = ProcessorUtils.parseAttributeValues(context, tag);
    String field = text(attributes, "field");
    if (field != null) {
      applyBinding(context, field, attributes);
    }
    String id = text(attributes, "id");
    if (id == null) {
      throw new TemplateProcessingException("govuk:select requires an id attribute");
    }

    String html = buildSelectHtml(id, attributes, buildOptions(attributes));
    final IModelFactory modelFactory = context.getModelFactory();
    final IModel model = modelFactory.parse(context.getTemplateData(), html);
    structureHandler.replaceWith(model, false);
  }

  /**
   * Binds like th:field; explicit attributes win.
   */
  private static void applyBinding(ITemplateContext context, String field,
                                   Map<String, Object> attributes) {
    IThymeleafBindStatus status = FieldUtils.getBindStatus(context, "*{" + field + "}");
    putIfBlank(attributes, "id", field);
    putIfBlank(attributes, "name", status.getExpression());
    putIfBlank(attributes, "value", status.getDisplayValue());
    if (status.isError()) {
      putIfBlank(attributes, "errorMessage", String.join(", ", status.getErrorMessages()));
    }
  }

  private static void putIfBlank(Map<String, Object> attributes, String key, Object value) {
    if (text(attributes, key) == null) {
      attributes.put(key, value);
    }
  }

  private String buildSelectHtml(String id, Map<String, Object> attributes,
                                 List<SelectOption> options) {
    final String hint = text(attributes, "hint");
    final String errorMessage = text(attributes, "errorMessage");

    StringBuilder html = new StringBuilder("<div class=\"govuk-form-group");
    if (errorMessage != null) {
      html.append(" govuk-form-group--error");
    }
    html.append("\">");

    appendLabel(html, id, attributes);

    List<String> describedBy = new ArrayList<>();
    if (hint != null) {
      describedBy.add(id + "-hint");
      html.append("<div id=\"").append(escape(id)).append("-hint\" class=\"govuk-hint\">")
          .append(escape(hint)).append("</div>");
    }
    if (errorMessage != null) {
      describedBy.add(id + "-error");
      html.append("<p id=\"").append(escape(id)).append("-error\" class=\"govuk-error-message\">")
          .append("<span class=\"govuk-visually-hidden\">Error:</span> ")
          .append(escape(errorMessage)).append("</p>");
    }

    appendSelectStartTag(html, id, attributes, errorMessage != null, describedBy);

    String selectedValue = Objects.requireNonNullElse(text(attributes, "value"), "");
    String placeholder = text(attributes, "placeholder");
    if (placeholder != null) {
      appendOption(html, new SelectOption("", placeholder), selectedValue);
    }
    for (SelectOption option : options) {
      appendOption(html, option, selectedValue);
    }

    return html.append("</select></div>").toString();
  }

  private static void appendLabel(StringBuilder html, String id, Map<String, Object> attributes) {
    String label = text(attributes, "label");
    if (label == null) {
      return;
    }
    String heading = text(attributes, "labelHeading");
    if (heading != null && !HEADINGS.contains(heading)) {
      throw new TemplateProcessingException(
          "govuk:select labelHeading must be one of h1 to h6 but was " + heading);
    }
    if (heading != null) {
      html.append("<").append(heading).append(" class=\"govuk-label-wrapper\">");
    }
    html.append("<label class=\"govuk-label");
    String labelClasses = text(attributes, "labelClasses");
    if (labelClasses != null) {
      html.append(" ").append(escape(labelClasses));
    }
    html.append("\" for=\"").append(escape(id)).append("\">")
        .append(escape(label)).append("</label>");
    if (heading != null) {
      html.append("</").append(heading).append(">");
    }
  }

  private static void appendSelectStartTag(StringBuilder html, String id,
                                           Map<String, Object> attributes, boolean hasError,
                                           List<String> describedBy) {
    html.append("<select class=\"govuk-select");
    if (hasError) {
      html.append(" govuk-select--error");
    }
    String classes = text(attributes, "classes");
    if (classes != null) {
      html.append(" ").append(escape(classes));
    }
    String name = Objects.requireNonNullElse(text(attributes, "name"), id);
    html.append("\" id=\"").append(escape(id))
        .append("\" name=\"").append(escape(name)).append("\"")
        // Stops the browser restoring the select on back navigation out of step with the input.
        .append(" autocomplete=\"off\"");
    if (!describedBy.isEmpty()) {
      html.append(" aria-describedby=\"").append(escape(String.join(" ", describedBy)))
          .append("\"");
    }
    html.append(" data-module=\"").append(DATA_MODULE).append("\"")
        .append(" data-show-all-values=\"").append(flag(attributes, "showAllValues"))
        .append("\"");
    appendDataAttributes(html, attributes);
    if (flag(attributes, "disabled")) {
      html.append(" disabled");
    }
    html.append(">");
  }

  private List<SelectOption> buildOptions(Map<String, Object> attributes) {
    Object items = attributes.get("items");
    String itemValue = text(attributes, "itemValue");
    String itemLabel = text(attributes, "itemLabel");

    if (items == null) {
      return List.of();
    }
    if (items instanceof Map<?, ?> map && itemValue == null && itemLabel == null) {
      return map.entrySet().stream()
          .map(entry -> new SelectOption(asString(entry.getKey()), asString(entry.getValue())))
          .toList();
    }

    List<SelectOption> options = new ArrayList<>();
    for (Object item : asIterable(items)) {
      String value = asString(itemValue == null ? item : property(item, itemValue));
      String label = itemLabel == null ? value : asString(property(item, itemLabel));
      options.add(new SelectOption(value, label));
    }
    return options;
  }

  private static Iterable<?> asIterable(Object items) {
    if (items instanceof Iterable<?> iterable) {
      return iterable;
    }
    if (items instanceof Map<?, ?> map) {
      return map.values();
    }
    if (items instanceof Object[] array) {
      return Arrays.asList(array);
    }
    throw new TemplateProcessingException(
        "govuk:select items must be a collection, array or map but was " + items.getClass());
  }

  private static Object property(Object item, String propertyName) {
    if (item instanceof Map<?, ?> map) {
      return map.get(propertyName);
    }
    return new BeanWrapperImpl(item).getPropertyValue(propertyName);
  }

  private static void appendOption(StringBuilder html, SelectOption option,
                                   String selectedValue) {
    html.append("<option value=\"").append(escape(option.value())).append("\"");
    if (option.value().equals(selectedValue)) {
      html.append(" selected");
    }
    html.append(">").append(escape(option.label())).append("</option>");
  }

  private static void appendDataAttributes(StringBuilder html, Map<String, Object> attributes) {
    attributes.entrySet().stream()
        .filter(entry -> entry.getKey().startsWith("data-"))
        .filter(entry -> !RESERVED_DATA_ATTRIBUTES.contains(entry.getKey()))
        .sorted(Map.Entry.comparingByKey())
        .forEach(entry -> html.append(" ").append(escape(entry.getKey())).append("=\"")
            .append(escape(asString(entry.getValue()))).append("\""));
  }

  /**
   * A bare attribute, or one repeating its own name, is true.
   */
  private static boolean flag(Map<String, Object> attributes, String key) {
    if (!attributes.containsKey(key)) {
      return false;
    }
    Object value = attributes.get(key);
    if (value instanceof Boolean bool) {
      return bool;
    }
    String text = value == null ? "" : value.toString();
    return text.isBlank() || text.equalsIgnoreCase(key) || Boolean.parseBoolean(text);
  }

  private static String text(Map<String, Object> attributes, String key) {
    Object value = attributes.get(key);
    return value == null || value.toString().isBlank() ? null : value.toString();
  }

  private static String asString(Object value) {
    return value == null ? "" : value.toString();
  }

  private static String escape(String value) {
    return HtmlEscape.escapeHtml(value, HtmlEscapeType.HTML5_NAMED_REFERENCES_DEFAULT_TO_DECIMAL,
        HtmlEscapeLevel.LEVEL_1_ONLY_MARKUP_SIGNIFICANT);
  }

  private record SelectOption(String value, String label) {
  }
}
