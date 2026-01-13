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
      JsonNode uriNode = getCaseInsensitiveField(node, "uri");
      if (uriNode == null || uriNode.isNull() || uriNode.asText().isBlank()) {
        throw InvalidFormatException.from(
            parser,
            "Authorized role uri entries must include a non-empty 'uri' field.",
            node,
            AuthorizedRoleUri.class);
      }
      String uri = uriNode.asText();

      JsonNode methodsNode = getCaseInsensitiveField(node, "method");
      if (methodsNode == null) {
        methodsNode = getCaseInsensitiveField(node, "methods");
      }
      String[] methods = null;
      if (methodsNode != null && !methodsNode.isNull()) {
        if (methodsNode.isArray()) {
          List<String> values = new ArrayList<>();
          for (JsonNode methodNode : methodsNode) {
            values.add(methodNode.asText());
          }
          methods = values.toArray(new String[0]);
        } else {
          methods = new String[] { methodsNode.asText() };
        }
      }

      return new AuthorizedRoleUri(uri, methods);
    }

    return (AuthorizedRoleUri) context.handleUnexpectedToken(AuthorizedRoleUri.class, parser);
  }

  private JsonNode getCaseInsensitiveField(JsonNode node, String fieldName) {
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
}
