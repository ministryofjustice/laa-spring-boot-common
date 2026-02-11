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
 * Generated export endpoint for library_books_optional_filters.
 */
@RestController
@RequestMapping("${laa.springboot.starter.exports.web.base-path:/exports}")
public class LibraryBooksOptionalFiltersExportController {
  private final ExportService exportService;

  public LibraryBooksOptionalFiltersExportController(ExportService exportService) {
    this.exportService = exportService;
  }

  @Operation(summary = "Export library_books_optional_filters")
  @ApiResponse(
      responseCode = "200",
      description = "CSV export",
      content = @Content(
          mediaType = "text/csv",
          examples = @ExampleObject(
              value =
                  "Book ID,Title,Genre,Status,Created On"
          )
      )
  )
  @GetMapping(value = "/library_books_optional_filters.csv", produces = "text/csv")
  public ResponseEntity<StreamingResponseBody> exportLibraryBooksOptionalFilters(
      @RequestParam(name = "genre", required = false) String genre,
      @RequestParam(name = "status", required = false) String status,
      @RequestParam(name = "createdOnOrAfter", required = false) String createdOnOrAfter
  ) {
    Map<String, String[]> rawParams = new HashMap<>();
    if (genre != null) {
      rawParams.put("genre", new String[] { genre });
    }
    if (status != null) {
      rawParams.put("status", new String[] { status });
    }
    if (createdOnOrAfter != null) {
      rawParams.put("createdOnOrAfter", new String[] { createdOnOrAfter });
    }
    String filename = "library_books_optional_filters-" + LocalDate.now() + ".csv";
    StreamingResponseBody body = out -> exportService.streamCsv("library_books_optional_filters", rawParams, out);
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
        .header(HttpHeaders.CACHE_CONTROL, "no-store")
        .body(body);
  }
}
