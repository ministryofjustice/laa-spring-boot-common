package uk.gov.laa.springboot.export.generated;

import jakarta.annotation.Generated;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.stereotype.Component;
import uk.gov.laa.springboot.export.ExportCsvProvider;
import uk.gov.laa.springboot.export.csv.CsvHeaderWriter;
import uk.gov.laa.springboot.export.datasource.postgres.PostgresCopyExporter;
import uk.gov.laa.springboot.export.model.ExportColumn;
import uk.gov.laa.springboot.export.model.ValidatedExportRequest;

/**
 * Export provider for library_books.
 */
@Component("libraryBooksProvider")
@Generated("export-sql-codegen")
public class LibraryBooksProvider implements ExportCsvProvider {
  private static final String SQL =
      String.join("\n",
          "select id as book_id, status as status from library_books where id > :minId"
      );
  private static final List<String> COLUMN_ORDER =
      List.of(
          "book_id",
          "status"
      );
  private final PostgresCopyExporter copyExporter;

  public LibraryBooksProvider(DataSource dataSource) {
    this.copyExporter = new PostgresCopyExporter(dataSource);
  }

  @Override
  public long writeCsv(
      ValidatedExportRequest request,
      OutputStream out,
      List<ExportColumn> columns) {
    Map<String, Object> params = new HashMap<>();
    params.putAll(request.getParams());
    params.put("maxRows", request.getMaxRows());
    try (Writer writer = new OutputStreamWriter(out, StandardCharsets.UTF_8)) {
      boolean hasOverrides = columns != null && !columns.isEmpty();
      if (hasOverrides) {
        CsvHeaderWriter.writeHeader(writer, COLUMN_ORDER, columns);
      }
      boolean includeHeader = !hasOverrides;
      long rows = copyExporter.copyCsv(SQL, params, writer, includeHeader);
      writer.flush();
      return rows;
    } catch (Exception e) {
      throw new RuntimeException("CSV export failed", e);
    }
  }
}
