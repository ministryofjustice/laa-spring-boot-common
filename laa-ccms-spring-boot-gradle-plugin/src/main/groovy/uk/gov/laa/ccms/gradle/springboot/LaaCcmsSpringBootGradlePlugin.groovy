package uk.gov.laa.ccms.gradle.springboot

import io.spring.gradle.dependencymanagement.DependencyManagementPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.springframework.boot.gradle.plugin.SpringBootPlugin
import uk.gov.laa.ccms.gradle.LaaCcmsJavaGradlePlugin

class LaaCcmsSpringBootGradlePlugin implements Plugin<Project> {

  public static final String BOM_COORDINATES = "uk.gov.laa.ccms.springboot:laa-ccms-spring-boot-dependencies:" +
          LaaCcmsSpringBootGradlePlugin.class.getPackage().getImplementationVersion()

  @Override
  void apply(Project target) {

    target.pluginManager.apply LaaCcmsJavaGradlePlugin
    target.pluginManager.apply SpringBootPlugin
    target.pluginManager.apply DependencyManagementPlugin

    target.dependencyManagement {
      imports {
        mavenBom LaaCcmsSpringBootGradlePlugin.BOM_COORDINATES
      }
    }

    target.springBoot {
      buildInfo()
    }

  }
}
