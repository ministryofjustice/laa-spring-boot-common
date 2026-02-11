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
 * Export provider for library_monthly_checkout_totals.
 */
@Component("libraryMonthlyCheckoutTotalsProvider")
@Generated("export-sql-codegen")
public class LibraryMonthlyCheckoutTotalsProvider implements ExportCsvProvider {
  private static final String SQL =
      String.join("\n",
          "select",
          "  date_trunc('month', l.loan_date)::date as checkoutMonth,",
          "  l.branch_code as branchCode,",
          "  count(*) as checkoutCount,",
          "  count(distinct l.member_id) as uniqueMembers",
          "from library.loans l",
          "where l.loan_date >= :fromDate",
          "  and l.loan_date < :toDate",
          "group by date_trunc('month', l.loan_date)::date, l.branch_code",
          "order by checkoutMonth, branchCode",
          "limit :maxRows"
      );
  private static final List<String> COLUMN_ORDER =
      List.of(
          "checkoutMonth",
          "branchCode",
          "checkoutCount",
          "uniqueMembers"
      );
  private final PostgresCopyExporter copyExporter;

  public LibraryMonthlyCheckoutTotalsProvider(DataSource dataSource) {
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
