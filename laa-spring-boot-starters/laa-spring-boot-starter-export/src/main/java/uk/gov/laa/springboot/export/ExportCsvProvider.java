package uk.gov.laa.springboot.export;

import java.io.OutputStream;
import java.util.List;
import uk.gov.laa.springboot.export.model.ExportColumn;
import uk.gov.laa.springboot.export.model.ValidatedExportRequest;

/**
 * Streams CSV output directly for an export request.
 */
public interface ExportCsvProvider {
  /**
   * Writes CSV to the output stream and returns the number of data rows written,
   * or -1 if the count is unknown.
   */
  long writeCsv(
      ValidatedExportRequest request,
      OutputStream out,
      List<ExportColumn> columns);
}
