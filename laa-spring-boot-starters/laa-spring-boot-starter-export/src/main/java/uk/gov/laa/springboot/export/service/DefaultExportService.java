package uk.gov.laa.springboot.export.service;

import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import uk.gov.laa.springboot.export.ExportAuditSink;
import uk.gov.laa.springboot.export.ExportCsvProvider;
import uk.gov.laa.springboot.export.ExportRegistry;
import uk.gov.laa.springboot.export.ExportRequestValidator;
import uk.gov.laa.springboot.export.ExportService;
import uk.gov.laa.springboot.export.model.ExportAuditEvent;
import uk.gov.laa.springboot.export.model.ExportDefinition;
import uk.gov.laa.springboot.export.model.ValidatedExportRequest;

/**
 * Default export service implementation.
 */
public final class DefaultExportService implements ExportService {
  private final ExportRegistry registry;
  private final ExportRequestValidator validator;
  private final ExportAuditSink audit;

  /**
   * Constructor for default export service.
   */
  public DefaultExportService(
      ExportRegistry registry,
      ExportRequestValidator validator,
      ExportAuditSink audit) {
    this.registry = registry;
    this.validator = validator;
    this.audit = audit;
  }

  /**
   * Streams a CSV export for the given key and raw parameters.
   */
  @Override
  public ValidatedExportRequest validateRequest(String exportKey, Map<String, String[]> rawParams) {
    ExportDefinition def = registry.getRequired(exportKey);
    return validator.validate(def, rawParams);
  }

  /**
   * Streams a CSV export for the given key and raw parameters.
   */
  @Override
  public void streamCsv(String exportKey, Map<String, String[]> rawParams, OutputStream out) {
    ValidatedExportRequest validated = validateRequest(exportKey, rawParams);
    streamCsv(exportKey, validated, out);
  }

  /**
   * Streams a CSV export for a pre-validated request.
   */
  @Override
  public void streamCsv(String exportKey, ValidatedExportRequest validated, OutputStream out) {
    ExportDefinition def = registry.getRequired(exportKey);

    long start = System.currentTimeMillis();
    AtomicLong rowCounter = new AtomicLong();

    try {
      ExportCsvProvider provider = registry.getProvider(exportKey);
      long rows = provider.writeCsv(validated, out, def.getColumns());
      if (rows >= 0) {
        rowCounter.set(rows);
      }

      audit.record(ExportAuditEvent.success(exportKey, validated, rowCounter.get(), start));
    } catch (Exception e) {
      audit.record(ExportAuditEvent.failure(exportKey, validated, rowCounter.get(), start, e));
      throw e;
    }
  }
}
