package uk.gov.laa.springboot.export.registry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.GenericApplicationContext;
import uk.gov.laa.springboot.export.ExportConfigurationException;
import uk.gov.laa.springboot.export.ExportCsvProvider;
import uk.gov.laa.springboot.export.config.LaaExportsProperties;
import uk.gov.laa.springboot.export.model.ExportDefinition;
import uk.gov.laa.springboot.export.model.ExportParamDefinition;
import uk.gov.laa.springboot.export.model.ValidatedExportRequest;

class DefaultExportRegistryTest {

  @Test
  void buildsRegistryFromPropertiesDefinitions() {
    GenericApplicationContext context = new GenericApplicationContext();
    context.registerBean("resourceProvider", ExportCsvProvider.class, NoOpProvider::new);
    context.registerBean("libraryProvider", ExportCsvProvider.class, NoOpProvider::new);
    context.refresh();

    LaaExportsProperties properties = new LaaExportsProperties();
    properties.getDefaults().setMaxRows(1200);

    LaaExportsProperties.Definition definition = new LaaExportsProperties.Definition();
    definition.setProvider("libraryProvider");
    definition.setDescription("Library books export");
    definition.setMaxRows(300);
    definition.setSql("select 1 as id");
    properties.setDefinitions(Map.of("library-books", definition));

    DefaultExportRegistry registry = new DefaultExportRegistry(context, properties);

    ExportDefinition exportDefinition = registry.getRequired("library-books");
    assertThat(exportDefinition.getProvider()).isEqualTo("libraryProvider");
    assertThat(exportDefinition.getMaxRows()).isEqualTo(300);
    assertThat(registry.getProvider("library-books")).isInstanceOf(NoOpProvider.class);
    assertThat(registry.keys()).contains("library-books");
  }

  @Test
  void loadsDefinitionsFromClasspathResourcesAndPropertiesOverrideResourceValues() {
    GenericApplicationContext context = new GenericApplicationContext();
    context.registerBean("resourceProvider", ExportCsvProvider.class, NoOpProvider::new);
    context.registerBean("overrideProvider", ExportCsvProvider.class, NoOpProvider::new);
    context.refresh();

    LaaExportsProperties properties = new LaaExportsProperties();
    properties.getDefaults().setMaxRows(50);

    LaaExportsProperties.Definition overrideDefinition = new LaaExportsProperties.Definition();
    overrideDefinition.setProvider("overrideProvider");
    overrideDefinition.setDescription("Override export");
    overrideDefinition.setSql("select 2 as id");
    properties.setDefinitions(Map.of("resource_export", overrideDefinition));

    DefaultExportRegistry registry = new DefaultExportRegistry(context, properties);

    ExportDefinition def = registry.getRequired("resource_export");
    assertThat(def.getProvider()).isEqualTo("overrideProvider");
    assertThat(def.getDescription()).isEqualTo("Override export");
  }

  @Test
  void throwsWhenProviderMissing() {
    GenericApplicationContext context = new GenericApplicationContext();
    context.registerBean("resourceProvider", ExportCsvProvider.class, NoOpProvider::new);
    context.refresh();

    LaaExportsProperties properties = new LaaExportsProperties();
    LaaExportsProperties.Definition definition = new LaaExportsProperties.Definition();
    definition.setProvider("missingProvider");
    definition.setSql("select 1 as id");
    properties.setDefinitions(Map.of("broken", definition));

    assertThatThrownBy(() -> new DefaultExportRegistry(context, properties))
        .isInstanceOf(ExportConfigurationException.class)
        .hasMessageContaining("missingProvider");
  }

  @Test
  void usesNameAsRequestNameByDefaultAndSupportsRequestNameOverride() {
    GenericApplicationContext context = new GenericApplicationContext();
    context.registerBean("resourceProvider", ExportCsvProvider.class, NoOpProvider::new);
    context.registerBean("libraryProvider", ExportCsvProvider.class, NoOpProvider::new);
    context.refresh();

    LaaExportsProperties properties = new LaaExportsProperties();
    LaaExportsProperties.Definition definition = new LaaExportsProperties.Definition();
    definition.setProvider("libraryProvider");
    definition.setSql("select 1 as id");

    LaaExportsProperties.Param automatic = new LaaExportsProperties.Param();
    automatic.setName("submissionId");
    automatic.setType("LONG");

    LaaExportsProperties.Param overridden = new LaaExportsProperties.Param();
    overridden.setName("accountId");
    overridden.setType("LONG");
    overridden.setRequestName("account-id");

    definition.setParams(java.util.List.of(automatic, overridden));
    properties.setDefinitions(Map.of("library-books", definition));

    DefaultExportRegistry registry = new DefaultExportRegistry(context, properties);

    ExportDefinition exportDefinition = registry.getRequired("library-books");
    ExportParamDefinition automaticParam = exportDefinition.getParams().get(0);
    ExportParamDefinition overriddenParam = exportDefinition.getParams().get(1);

    assertThat(automaticParam.getRequestName()).isEqualTo("submissionId");
    assertThat(overriddenParam.getRequestName()).isEqualTo("account-id");
  }

  private static final class NoOpProvider implements ExportCsvProvider {

    @Override
    public long writeCsv(
        ValidatedExportRequest request,
        java.io.OutputStream out,
        java.util.List<uk.gov.laa.springboot.export.model.ExportColumn> columns) {
      return 0;
    }
  }
}
