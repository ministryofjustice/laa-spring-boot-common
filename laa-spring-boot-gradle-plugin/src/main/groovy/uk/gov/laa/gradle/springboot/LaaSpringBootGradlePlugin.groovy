package uk.gov.laa.gradle.springboot

import io.spring.gradle.dependencymanagement.DependencyManagementPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.springframework.boot.gradle.plugin.SpringBootPlugin
import uk.gov.laa.gradle.LaaJavaGradlePlugin

class LaaSpringBootGradlePlugin implements Plugin<Project> {

  public static final String BOM_COORDINATES = "uk.gov.laa.springboot:laa-spring-boot-dependencies:" +
          LaaSpringBootGradlePlugin.class.getPackage().getImplementationVersion()

  @Override
  void apply(Project target) {

    target.pluginManager.apply LaaJavaGradlePlugin
    target.pluginManager.apply SpringBootPlugin
    target.pluginManager.apply DependencyManagementPlugin

    target.dependencyManagement {
      imports {
        mavenBom LaaSpringBootGradlePlugin.BOM_COORDINATES
      }
    }

    target.springBoot {
      buildInfo()
    }

  }
}
