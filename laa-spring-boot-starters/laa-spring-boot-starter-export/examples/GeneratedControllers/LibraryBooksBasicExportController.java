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

/**
 * Generated export endpoint for library_books_basic.
 */
@RestController
@RequestMapping("${laa.springboot.starter.exports.web.base-path:/exports}")
public class LibraryBooksBasicExportController {
  private final ExportService exportService;

  public LibraryBooksBasicExportController(ExportService exportService) {
    this.exportService = exportService;
  }

  @Operation(summary = "Export library_books_basic")
  @ApiResponse(
      responseCode = "200",
      description = "CSV export",
      content = @Content(
          mediaType = "text/csv",
          examples = @ExampleObject(
              value =
                  "Book ID,ISBN,Title,Status,Published On"
          )
      )
  )
  @GetMapping(value = "/library_books_basic.csv", produces = "text/csv")
  public ResponseEntity<StreamingResponseBody> exportLibraryBooksBasic(
      @RequestParam(name = "status", required = false) String status
  ) {
    Map<String, String[]> rawParams = new HashMap<>();
    if (status != null) {
      rawParams.put("status", new String[] { status });
    }
    String filename = "library_books_basic-" + LocalDate.now() + ".csv";
    StreamingResponseBody body = out -> exportService.streamCsv("library_books_basic", rawParams, out);
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
        .header(HttpHeaders.CACHE_CONTROL, "no-store")
        .body(body);
  }
}
