package uk.gov.laa.springboot.export.datasource.postgres;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PostgresSqlRendererTest {

  @Test
  void rendersNamedParamsWithSqlLiterals() {
    String sql =
        "select * from my_table where id=:id and active=:active and created_on=:created and type in (:types) and name=:name";

    String rendered =
        PostgresSqlRenderer.render(
            sql,
            Map.of(
                "id", UUID.fromString("11111111-1111-1111-1111-111111111111"),
                "active", true,
                "created", LocalDate.parse("2025-01-31"),
                "types", List.of("A", "B"),
                "name", "O'Hara"));

    assertThat(rendered)
        .contains("id='11111111-1111-1111-1111-111111111111'")
        .contains("active=TRUE")
        .contains("created_on='2025-01-31'")
        .contains("type in ('A', 'B')")
        .contains("name='O''Hara'");
  }

  @Test
  void keepsPostgresCastSyntaxAndRendersMissingParamAsNull() {
    String sql = "select now()::date as today where status=:status and optional=:missing";

    String rendered = PostgresSqlRenderer.render(sql, Map.of("status", "OPEN"));

    assertThat(rendered)
        .contains("now()::date")
        .contains("status='OPEN'")
        .contains("optional=NULL");
  }
}
