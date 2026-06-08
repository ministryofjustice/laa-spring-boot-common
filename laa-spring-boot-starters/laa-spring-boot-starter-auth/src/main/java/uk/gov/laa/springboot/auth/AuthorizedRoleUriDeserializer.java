package uk.gov.laa.springboot.auth;

import java.util.ArrayList;
import java.util.List;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.deser.std.StdDeserializer;
import tools.jackson.databind.exc.InvalidFormatException;

/**
 * Deserializes authorized role uri entries, supporting both string and object formats.
 */
public class AuthorizedRoleUriDeserializer extends StdDeserializer<AuthorizedRoleUri> {

  private static final String URI_FIELD = "uri";
  private static final String METHOD_FIELD = "method";
  private static final String METHODS_FIELD = "methods";

  protected AuthorizedRoleUriDeserializer() {
    super(AuthorizedRoleUri.class);
  }

  @Override
  public AuthorizedRoleUri deserialize(JsonParser parser, DeserializationContext context)
      throws JacksonException {
    JsonToken token = parser.currentToken();

    if (token == JsonToken.VALUE_STRING) {
      return new AuthorizedRoleUri(parser.getValueAsString(), null);
    }

    if (token == JsonToken.START_OBJECT) {
      JsonNode node = parser.readValueAsTree();
      JsonNode uriNode = getCaseInsensitiveField(node, URI_FIELD);
      if (uriNode == null || uriNode.isNull() || uriNode.asString().isBlank()) {
        throw InvalidFormatException.from(
            parser,
            "Authorized role uri entries must include a non-empty '" + URI_FIELD + "' field.",
            node,
            AuthorizedRoleUri.class);
      }
      String uri = uriNode.asString();

      JsonNode methodsNode = getCaseInsensitiveField(node, METHOD_FIELD);
      if (methodsNode == null) {
        methodsNode = getCaseInsensitiveField(node, METHODS_FIELD);
      }
      String[] methods = null;
      if (methodsNode != null && !methodsNode.isNull()) {
        if (methodsNode.isArray()) {
          List<String> values = new ArrayList<>();
          for (JsonNode methodNode : methodsNode) {
            addMethodValue(values, methodNode);
          }
          if (!values.isEmpty()) {
            methods = values.toArray(new String[0]);
          }
        } else {
          List<String> values = new ArrayList<>();
          addMethodValue(values, methodsNode);
          if (!values.isEmpty()) {
            methods = values.toArray(new String[0]);
          }
        }
      }

      return new AuthorizedRoleUri(uri, methods);
    }

    return (AuthorizedRoleUri) context.handleUnexpectedToken(AuthorizedRoleUri.class, parser);
  }

  private JsonNode getCaseInsensitiveField(JsonNode node, String fieldName) {
    if (node == null || fieldName == null) {
      return null;
    }
    JsonNode direct = node.get(fieldName);
    if (direct != null) {
      return direct;
    }
    for (String name : node.propertyNames()) {
      if (name.equalsIgnoreCase(fieldName)) {
        return node.get(name);
      }
    }
    return null;
  }

  private void addMethodValue(List<String> values, JsonNode methodNode) {
    if (methodNode == null || methodNode.isNull()) {
      return;
    }
    String method = methodNode.asString();
    if (method != null && !method.isBlank()) {
      values.add(method);
    }
  }
}
