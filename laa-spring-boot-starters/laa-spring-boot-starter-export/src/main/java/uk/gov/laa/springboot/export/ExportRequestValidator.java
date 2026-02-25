package uk.gov.laa.springboot.export;

import java.util.Map;
import uk.gov.laa.springboot.export.model.ExportDefinition;
import uk.gov.laa.springboot.export.model.ValidatedExportRequest;

/**
 * Validates and normalizes export requests.
 */
public interface ExportRequestValidator {
  ValidatedExportRequest validate(ExportDefinition def, Map<String, String[]> rawParams);
}
