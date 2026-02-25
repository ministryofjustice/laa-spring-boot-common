package uk.gov.laa.springboot.export.datasource.postgres;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.StringWriter;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Map;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.postgresql.ds.PGSimpleDataSource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class PostgresCopyExporterTest {

  @Container
  private static final PostgreSQLContainer<?> POSTGRES =
      new PostgreSQLContainer<>("postgres:16-alpine");

  private static DataSource dataSource;

  @BeforeAll
  static void setUpDatabase() throws Exception {
    POSTGRES.start();

    PGSimpleDataSource pgDataSource = new PGSimpleDataSource();
    pgDataSource.setURL(POSTGRES.getJdbcUrl());
    pgDataSource.setUser(POSTGRES.getUsername());
    pgDataSource.setPassword(POSTGRES.getPassword());
    dataSource = pgDataSource;

    try (Connection connection = dataSource.getConnection();
        Statement statement = connection.createStatement()) {
      statement.execute("create table export_people(id int primary key, name text)");
      statement.execute("insert into export_people(id, name) values (1, 'Alice'), (2, 'Bob')");
    }
  }

  @AfterAll
  static void tearDown() {
    POSTGRES.stop();
  }

  @Test
  void copyCsvExecutesCopyAndWritesCsv() {
    PostgresCopyExporter exporter = new PostgresCopyExporter(dataSource);
    StringWriter writer = new StringWriter();

    long rows =
        exporter.copyCsv(
            "select id as id, name as name from export_people where id <= :maxRows order by id",
            Map.of("maxRows", 2),
            writer,
            true);

    assertThat(rows).isGreaterThanOrEqualTo(0);
    assertThat(writer.toString()).contains("id,name").contains("1,Alice").contains("2,Bob");
  }

  @Test
  void copyCsvCanRunRepeatedlyWithoutHoldingConnectionsOpen() {
    PostgresCopyExporter exporter = new PostgresCopyExporter(dataSource);

    StringWriter first = new StringWriter();
    StringWriter second = new StringWriter();

    exporter.copyCsv("select id as id from export_people order by id", Map.of(), first, true);
    exporter.copyCsv("select id as id from export_people order by id", Map.of(), second, true);

    assertThat(first.toString()).contains("id").contains("1");
    assertThat(second.toString()).contains("id").contains("2");
  }
}
