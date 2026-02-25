package uk.gov.laa.springboot.export.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import uk.gov.laa.springboot.export.ExportValidationException;
import uk.gov.laa.springboot.export.model.ExportDefinition;
import uk.gov.laa.springboot.export.model.ExportParamDefinition;
import uk.gov.laa.springboot.export.model.ValidatedExportRequest;

class DefaultExportRequestValidatorTest {

  private final DefaultExportRequestValidator validator = new DefaultExportRequestValidator();

  @Test
  void validatesAndParsesParamsWithMaxRowsCap() {
    ExportDefinition definition =
        new ExportDefinition(
            "library-books",
            "Library books export",
            200,
            "libraryProvider",
            List.of(),
            List.of(
                new ExportParamDefinition("startDate", "DATE", null, List.of(), true, null),
                new ExportParamDefinition(
                    "status", "STRING", null, List.of("OPEN", "CLOSED"), false, "OPEN")));

    ValidatedExportRequest request =
        validator.validate(
            definition,
            Map.of("startDate", new String[] {"2025-02-01"}, "maxRows", new String[] {"999"}));

    assertThat(request.getParam("startDate", LocalDate.class)).contains(LocalDate.parse("2025-02-01"));
    assertThat(request.getParam("status", String.class)).contains("OPEN");
    assertThat(request.getMaxRows()).isEqualTo(200);
  }

  @Test
  void throwsForMissingRequiredParam() {
    ExportDefinition definition =
        new ExportDefinition(
            "library-books",
            "Library books export",
            200,
            "libraryProvider",
            List.of(),
            List.of(new ExportParamDefinition("startDate", "DATE", null, List.of(), true, null)));

    assertThatThrownBy(() -> validator.validate(definition, Map.of()))
        .isInstanceOf(ExportValidationException.class)
        .hasMessage("Missing required param: startDate");
  }

  @Test
  void throwsForInvalidMaxRowsFormat() {
    ExportDefinition definition =
        new ExportDefinition(
            "library-books",
            "Library books export",
            200,
            "libraryProvider",
            List.of(),
            List.of());

    assertThatThrownBy(
            () -> validator.validate(definition, Map.of("maxRows", new String[] {"not-a-number"})))
        .isInstanceOf(ExportValidationException.class)
        .hasMessage("maxRows must be an integer");
  }

  @Test
  void acceptsRequestNameAliasAndMapsToInternalName() {
    ExportDefinition definition =
        new ExportDefinition(
            "library-books",
            "Library books export",
            200,
            "libraryProvider",
            List.of(),
            List.of(
                new ExportParamDefinition(
                    "submissionId", "submission-id", "LONG", null, List.of(), true, null)));

    ValidatedExportRequest request =
        validator.validate(definition, Map.of("submission-id", new String[] {"123"}));

    assertThat(request.getParam("submissionId", Long.class)).contains(123L);
  }
}
