package uk.gov.laa.ccms.gradle.springboot;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

public class LaaCcmsSpringBootGradlePluginTest {

  @Test
  public void testApplyLaaCcmsSpringBootGradlePlugin() {
    LaaCcmsSpringBootGradlePlugin plugin = new LaaCcmsSpringBootGradlePlugin();
    Project project = ProjectBuilder.builder().build();
    plugin.apply(project);
  }
}
