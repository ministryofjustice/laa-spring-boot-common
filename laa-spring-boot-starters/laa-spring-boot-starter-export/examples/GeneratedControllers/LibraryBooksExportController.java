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
 * Generated export endpoint for library_books.
 */
@RestController
@RequestMapping("${laa.springboot.starter.exports.web.base-path:/exports}")
public class LibraryBooksExportController {
  private final ExportService exportService;

  public LibraryBooksExportController(ExportService exportService) {
    this.exportService = exportService;
  }

  @Operation(summary = "Export library_books")
  @ApiResponse(
      responseCode = "200",
      description = "CSV export",
      content = @Content(
          mediaType = "text/csv",
          examples = @ExampleObject(
              value =
                  "Book ID,Status"
          )
      )
  )
  @GetMapping(value = "/library_books.csv", produces = "text/csv")
  public ResponseEntity<StreamingResponseBody> exportLibraryBooks(
      @RequestParam(name = "minId") String minId
  ) {
    Map<String, String[]> rawParams = new HashMap<>();
    rawParams.put("minId", new String[] { minId });
    String filename = "library_books-" + LocalDate.now() + ".csv";
    ValidatedExportRequest validatedRequest = exportService.validateRequest("library_books", rawParams);
    StreamingResponseBody body = out -> exportService.streamCsv("library_books", validatedRequest, out);
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
        .header(HttpHeaders.CACHE_CONTROL, "no-store")
        .body(body);
  }
}
