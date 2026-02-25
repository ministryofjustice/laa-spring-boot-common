package uk.gov.laa.springboot.export.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.laa.springboot.export.ExportCsvProvider;
import uk.gov.laa.springboot.export.generated.LibraryBooksExportController;
import uk.gov.laa.springboot.export.model.ExportColumn;
import uk.gov.laa.springboot.export.model.ValidatedExportRequest;

@SpringBootTest(classes = ExportEndpointIntegrationTest.TestApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(
    properties = {
      "laa.springboot.starter.exports.enabled=true",
      "laa.springboot.starter.exports.definitions.library-books.provider=libraryBooksProvider",
      "laa.springboot.starter.exports.definitions.library-books.params[0].name=statusCode",
      "laa.springboot.starter.exports.definitions.library-books.params[0].type=INT",
      "laa.springboot.starter.exports.definitions.library-books.params[0].required=false"
    })
class ExportEndpointIntegrationTest {

  @org.springframework.beans.factory.annotation.Autowired private MockMvc mockMvc;

  @Test
  void streamsCsvForGeneratedControllerEndpoint() throws Exception {
    MvcResult asyncResult =
        mockMvc
            .perform(get("/exports/library-books").param("statusCode", "7"))
            .andExpect(request().asyncStarted())
            .andReturn();

    MvcResult response =
        mockMvc
            .perform(asyncDispatch(asyncResult))
            .andExpect(status().isOk())
            .andExpect(header().string("Cache-Control", "no-store"))
            .andExpect(
                header().string(
                    "Content-Disposition", containsString("filename=\"library-books-7-")))
            .andReturn();

    assertThat(response.getResponse().getContentAsString()).contains("statusCode").contains("7");
  }

  @Test
  void throwsValidationExceptionForInvalidExportParam() throws Exception {
    mockMvc
        .perform(get("/exports/library-books").param("statusCode", "not-an-int"))
        .andExpect(status().isBadRequest())
        .andExpect(content().string(org.hamcrest.Matchers.containsString("Filter statusCode must be an integer")));
  }

  @SpringBootApplication
  @Import(LibraryBooksExportController.class)
  static class TestApplication {

    @Bean("resourceProvider")
    ExportCsvProvider resourceProvider() {
      return (request, out, columns) -> 0L;
    }

    @Bean("libraryBooksProvider")
    ExportCsvProvider libraryBooksProvider() {
      return new ExportCsvProvider() {
        @Override
        public long writeCsv(
            ValidatedExportRequest request, OutputStream out, List<ExportColumn> columns)
            throws RuntimeException {
          String statusCode = String.valueOf(request.getParams().getOrDefault("statusCode", ""));
          String csv = "statusCode\\n" + statusCode + "\\n";
          try {
            out.write(csv.getBytes(StandardCharsets.UTF_8));
            return 1;
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        }
      };
    }
  }
}
