package uk.gov.laa.gradle.springboot.starter.export

import org.gradle.api.Plugin
import org.gradle.api.Project

class LaaSpringBootStarterExportCodegenGradlePlugin implements Plugin<Project> {

  @Override
  void apply(Project target) {
    SpringBootStarterExportCodegenTasks.registerAll(target)
  }
}
