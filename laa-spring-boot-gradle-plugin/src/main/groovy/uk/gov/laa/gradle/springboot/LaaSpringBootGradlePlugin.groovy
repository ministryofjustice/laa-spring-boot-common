package uk.gov.laa.gradle.springboot

import io.spring.gradle.dependencymanagement.DependencyManagementPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.springframework.boot.gradle.plugin.SpringBootPlugin
import uk.gov.laa.gradle.LaaJavaGradlePlugin
import uk.gov.laa.gradle.springboot.starter.export.SpringBootStarterExportCodegenTasks

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

    target.afterEvaluate {
      if (hasExportStarterDependency(target)) {
        SpringBootStarterExportCodegenTasks.registerAll(target)
      }
    }

  }

  private static boolean hasExportStarterDependency(Project target) {
    target.configurations.any { cfg ->
      cfg.dependencies.any { dep -> isExportStarter(dep) }
    }
  }

  private static boolean isExportStarter(Dependency dep) {
    if (dep == null) {
      return false
    }
    return dep.name == 'laa-spring-boot-starter-export'
        && (dep.group == null || dep.group == 'uk.gov.laa.springboot')
  }
}
