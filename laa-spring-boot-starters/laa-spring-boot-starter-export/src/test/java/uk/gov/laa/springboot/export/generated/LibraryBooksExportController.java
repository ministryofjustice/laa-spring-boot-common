package uk.gov.laa.springboot.export.generated;

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

@RestController
@RequestMapping("${laa.springboot.starter.exports.web.base-path:/exports}")
public class LibraryBooksExportController {

  private final ExportService exportService;

  public LibraryBooksExportController(ExportService exportService) {
    this.exportService = exportService;
  }

  @GetMapping(value = "/library-books.csv", produces = "text/csv")
  public ResponseEntity<StreamingResponseBody> exportLibraryBooks(
      @RequestParam(name = "status", required = false) String status) {
    Map<String, String[]> rawParams = new HashMap<>();
    if (status != null) {
      rawParams.put("status", new String[] {status});
    }

    StringBuilder filename = new StringBuilder("library-books");
    if (status != null && !status.isBlank()) {
      filename.append("-").append(sanitizeFilenamePart(status));
    }
    filename.append("-").append(LocalDate.now()).append(".csv");
    String outputFilename = filename.toString();
    ValidatedExportRequest validatedRequest = exportService.validateRequest("library-books", rawParams);
    StreamingResponseBody body = out -> exportService.streamCsv("library-books", validatedRequest, out);

    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + outputFilename + "\"")
        .header(HttpHeaders.CACHE_CONTROL, "no-store")
        .body(body);
  }

  private String sanitizeFilenamePart(String value) {
    return value.replaceAll("[^A-Za-z0-9._-]", "_");
  }
}
