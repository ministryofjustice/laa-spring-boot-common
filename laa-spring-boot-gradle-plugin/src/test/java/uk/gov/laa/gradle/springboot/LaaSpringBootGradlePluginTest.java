package uk.gov.laa.gradle.springboot;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

public class LaaSpringBootGradlePluginTest {

  @Test
  public void testApplyLaaSpringBootGradlePlugin() {
    LaaSpringBootGradlePlugin plugin = new LaaSpringBootGradlePlugin();
    Project project = ProjectBuilder.builder().build();
    plugin.apply(project);
  }

  @Test
  public void importsDependenciesFromVerifiedMavenCentralNamespace() {
    assertTrue(
        LaaSpringBootGradlePlugin.BOM_COORDINATES.startsWith(
            "uk.gov.justice.service.laa:laa-spring-boot-dependencies:"));
  }

}
