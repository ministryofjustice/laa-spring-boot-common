package uk.gov.laa.springboot.export;

import java.io.OutputStream;
import java.util.Map;
import uk.gov.laa.springboot.export.model.ValidatedExportRequest;

/**
 * Streams export data to an output stream.
 */
public interface ExportService {
  /**
   * Validates raw request parameters for an export key.
   */
  ValidatedExportRequest validateRequest(String exportKey, Map<String, String[]> rawParams);

  /**
   * Streams a validated request to CSV.
   */
  void streamCsv(String exportKey, ValidatedExportRequest request, OutputStream out);

  /**
   * Validates and streams raw request parameters.
   */
  void streamCsv(String exportKey, Map<String, String[]> rawParams, OutputStream out);
}
