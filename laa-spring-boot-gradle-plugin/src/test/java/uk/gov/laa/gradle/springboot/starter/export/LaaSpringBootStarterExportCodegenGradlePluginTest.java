package uk.gov.laa.gradle.springboot.starter.export;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

public class LaaSpringBootStarterExportCodegenGradlePluginTest {

  @Test
  public void testApplyLaaSpringBootStarterExportCodegenGradlePlugin() {
    LaaSpringBootStarterExportCodegenGradlePlugin plugin =
        new LaaSpringBootStarterExportCodegenGradlePlugin();
    Project project = ProjectBuilder.builder().build();
    plugin.apply(project);
  }
}
