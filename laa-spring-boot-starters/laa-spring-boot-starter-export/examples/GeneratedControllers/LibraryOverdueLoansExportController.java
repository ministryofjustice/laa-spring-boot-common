package uk.gov.laa.springboot.export.generated;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import uk.gov.laa.springboot.export.ExportService;
import uk.gov.laa.springboot.export.model.ValidatedExportRequest;

/**
 * Generated export endpoint for library_overdue_loans.
 */
@RestController
@RequestMapping("${laa.springboot.starter.exports.web.base-path:/exports}")
public class LibraryOverdueLoansExportController {
  private final ExportService exportService;

  public LibraryOverdueLoansExportController(ExportService exportService) {
    this.exportService = exportService;
  }

  @Operation(summary = "Export library_overdue_loans")
  @ApiResponse(
      responseCode = "200",
      description = "CSV export",
      content = @Content(
          mediaType = "text/csv",
          examples = @ExampleObject(
              value =
                  "Loan ID,Member Number,Member Name,Book Title,Loan Date,Due Date,Days Overdue"
          )
      )
  )
  @GetMapping(value = "/library_overdue_loans.csv", produces = "text/csv")
  public ResponseEntity<StreamingResponseBody> exportLibraryOverdueLoans(
      @RequestParam(name = "asOfDate") String asOfDate,
      @RequestParam(name = "branchCode", required = false) String branchCode
  ) {
    Map<String, String[]> rawParams = new HashMap<>();
    if (asOfDate != null) {
      rawParams.put("asOfDate", new String[] { asOfDate });
    }
    if (branchCode != null) {
      rawParams.put("branchCode", new String[] { branchCode });
    }
    String filename = "library_overdue_loans-" + LocalDate.now() + ".csv";
    ValidatedExportRequest validatedRequest = exportService.validateRequest("library_overdue_loans", rawParams);
    StreamingResponseBody body = out -> exportService.streamCsv("library_overdue_loans", validatedRequest, out);
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
        .header(HttpHeaders.CACHE_CONTROL, "no-store")
        .body(body);
  }
}
