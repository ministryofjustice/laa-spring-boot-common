package uk.gov.laa.springboot.export;

import java.util.Set;
import uk.gov.laa.springboot.export.model.ExportDefinition;

/**
 * Resolves export definitions and providers.
 */
public interface ExportRegistry {
  ExportDefinition getRequired(String key);

  ExportCsvProvider getProvider(String key);

  Set<String> keys();
}
