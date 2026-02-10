package uk.gov.laa.springboot.export.csv;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.StringWriter;
import java.util.List;
import org.junit.jupiter.api.Test;
import uk.gov.laa.springboot.export.model.ExportColumn;

class CsvHeaderWriterTest {

  @Test
  void writesHeadersUsingConfiguredOrderAndOverrides() throws Exception {
    StringWriter writer = new StringWriter();

    CsvHeaderWriter.writeHeader(
        writer,
        List.of("id", "first_name", "last_name"),
        List.of(
            new ExportColumn("first_name", "First Name", null),
            new ExportColumn("last_name", "Last, Name", null)));

    assertThat(writer.toString()).isEqualTo("id,First Name,\"Last, Name\"\n");
  }

  @Test
  void appendsNonOrderedOverrideColumns() throws Exception {
    StringWriter writer = new StringWriter();

    CsvHeaderWriter.writeHeader(
        writer,
        List.of("id"),
        List.of(new ExportColumn("extra", "Extra Header", null)));

    assertThat(writer.toString()).isEqualTo("id,Extra Header\n");
  }
}
