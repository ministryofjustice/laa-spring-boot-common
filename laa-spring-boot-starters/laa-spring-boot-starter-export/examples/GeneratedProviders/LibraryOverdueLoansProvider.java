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
 * Export provider for library_overdue_loans.
 */
@Component("libraryOverdueLoansProvider")
@Generated("export-sql-codegen")
public class LibraryOverdueLoansProvider implements ExportCsvProvider {
  private static final String SQL =
      String.join("\n",
          "select",
          "  l.id as loanId,",
          "  m.membership_number as memberNumber,",
          "  m.full_name as memberName,",
          "  b.title as bookTitle,",
          "  l.loan_date as loanDate,",
          "  l.due_date as dueDate,",
          "  (current_date - l.due_date) as daysOverdue",
          "from library.loans l",
          "join library.members m on m.id = l.member_id",
          "join library.books b on b.id = l.book_id",
          "where l.returned_on is null",
          "  and l.due_date < :asOfDate",
          "  and (:branchCode is null or l.branch_code = :branchCode)",
          "order by daysOverdue desc, l.due_date",
          "limit :maxRows"
      );
  private static final List<String> COLUMN_ORDER =
      List.of(
          "loanId",
          "memberNumber",
          "memberName",
          "bookTitle",
          "loanDate",
          "dueDate",
          "daysOverdue"
      );
  private final PostgresCopyExporter copyExporter;

  public LibraryOverdueLoansProvider(DataSource dataSource) {
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
