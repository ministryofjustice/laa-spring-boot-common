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
 * Generated export endpoint for library_monthly_checkout_totals.
 */
@RestController
@RequestMapping("${laa.springboot.starter.exports.web.base-path:/exports}")
public class LibraryMonthlyCheckoutTotalsExportController {
  private final ExportService exportService;

  public LibraryMonthlyCheckoutTotalsExportController(ExportService exportService) {
    this.exportService = exportService;
  }

  @Operation(summary = "Export library_monthly_checkout_totals")
  @ApiResponse(
      responseCode = "200",
      description = "CSV export",
      content = @Content(
          mediaType = "text/csv",
          examples = @ExampleObject(
              value =
                  "Checkout Month,Branch,Checkouts,Unique Members"
          )
      )
  )
  @GetMapping(value = "/library_monthly_checkout_totals.csv", produces = "text/csv")
  public ResponseEntity<StreamingResponseBody> exportLibraryMonthlyCheckoutTotals(
      @RequestParam(name = "fromDate") String fromDate,
      @RequestParam(name = "toDate") String toDate
  ) {
    Map<String, String[]> rawParams = new HashMap<>();
    if (fromDate != null) {
      rawParams.put("fromDate", new String[] { fromDate });
    }
    if (toDate != null) {
      rawParams.put("toDate", new String[] { toDate });
    }
    String filename = "library_monthly_checkout_totals-" + LocalDate.now() + ".csv";
    ValidatedExportRequest validatedRequest = exportService.validateRequest("library_monthly_checkout_totals", rawParams);
    StreamingResponseBody body = out -> exportService.streamCsv("library_monthly_checkout_totals", validatedRequest, out);
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
        .header(HttpHeaders.CACHE_CONTROL, "no-store")
        .body(body);
  }
}
