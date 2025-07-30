package uk.gov.laa.gradle.springboot;

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
}
