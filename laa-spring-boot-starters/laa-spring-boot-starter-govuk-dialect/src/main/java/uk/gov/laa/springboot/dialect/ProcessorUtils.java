package uk.gov.laa.springboot.dialect;

import java.util.HashMap;
import java.util.Map;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.standard.expression.IStandardExpression;
import org.thymeleaf.standard.expression.IStandardExpressionParser;
import org.thymeleaf.standard.expression.StandardExpressions;

/**
 * ProcessorUtils for common code.
 */
public class ProcessorUtils {

  private ProcessorUtils() {
  }

  /**
   * Evaluate thymeleaf expressions.
   */
  public static Map<String, String> parseAttributes(ITemplateContext context,
                                                    IProcessableElementTag tag) {
    Map<String, String> attributes = tag.getAttributeMap();
    Map<String, String> resolvedAttributes = new HashMap<>();
    IStandardExpressionParser parser =
        StandardExpressions.getExpressionParser(context.getConfiguration());

    for (Map.Entry<String, String> entry : attributes.entrySet()) {
      String key = entry.getKey();
      String value = entry.getValue();
      if (key.startsWith("th:")) {
        IStandardExpression expression = parser.parseExpression(context, value);
        resolvedAttributes.put(key.replace("th:", ""), (String) expression.execute(context));
      } else {
        resolvedAttributes.put(key, value);
      }
    }

    return resolvedAttributes;
  }

}
