# LAA CCMS Spring Boot Common

Provides 2 plugins that configure plugins and apply common build logic,
and a set of starters that provide individual pieces of common functionality.

## Available Plugins

### `laa-ccms-java-gradle-plugin` for Java Projects

A general purpose Java plugin for LAA CCMS projects.

  - apply [Java](https://docs.gradle.org/current/userguide/java_plugin.html) plugin, and configure a Java toolchain.
  - apply [Jacoco](https://docs.gradle.org/current/userguide/jacoco_plugin.html) plugin, and configure sensible defaults.
  - apply [Versions](https://github.com/ben-manes/gradle-versions-plugin) plugin, and configure the recommended versioning strategy.
  - apply [Checkstyle](https://docs.gradle.org/current/userguide/checkstyle_plugin.html) plugin, and configure sensible defaults.
  - apply [Maven Publish](https://docs.gradle.org/current/userguide/publishing_maven.html) plugin, and configure LAA CCMS repositories and credential resolution for local development and pipelines. For publishing, the repository name can be overridden by setting the `repositoryName` property in your `gradle.properties`. This is helpful when your repository name is different from your project name.
  - apply [Gradle Release](https://github.com/researchgate/gradle-release) plugin, and define a release tag format.

In addition to this an `integrationTest` gradle task will be provided, that will run tests under `src/main/integrationTest`. All test tasks will also output increased logging (standard streams and stack traces) to aid with debugging.

```groovy
plugins {
    id 'uk.gov.laa.ccms.laa-ccms-java-gradle-plugin' version '<latest>'
}
```

### `laa-ccms-spring-boot-gradle-plugin` for Java + Spring Boot projects

A SpringBoot convention plugin for LAA CCMS projects. All of the above + SpringBoot dependency version recommendations to simplify dependency management and avoid compatibility issues within a project.

  - apply the [LAA CCMS Java Gradle](#laa-ccms-java-gradle-plugin-for-java-projects) plugin
  - apply the [SpringBoot](https://plugins.gradle.org/plugin/org.springframework.boot) plugin
  - apply the [Dependency Management](https://plugins.gradle.org/plugin/io.spring.dependency-management) plugin, and configure dependency management for the common LAA CCMS Spring Boot components (starters & libraries)

```groovy
plugins {
    id 'uk.gov.laa.ccms.laa-ccms-spring-boot-gradle-plugin' version '<latest>'
}
```

## Using the Plugins

For the plugins to work in your project, you will need to configure the plugin repository and provide your GitHub credentials in your local `gradle.properties` file.

### Define the Plugin repository

To configure the plugin repository, add this **to the top** your project's `settings.gradle`:

```groovy
pluginManagement {
    repositories {
        maven {
            name = "gitHubPackages"
            url uri('https://maven.pkg.github.com/ministryofjustice/laa-ccms-spring-boot-common')
            credentials {
                username = System.getenv("GITHUB_ACTOR")?.trim() ?: settings.ext.find('project.ext.gitPackageUser')
                password = System.getenv("GITHUB_TOKEN")?.trim() ?: settings.ext.find('project.ext.gitPackageKey')
            }
        }
        maven { url "https://plugins.gradle.org/m2/" }
        gradlePluginPortal()
    }
}
```

This tells Gradle where to search for plugins. The plugins in this repository are published to GitHub packages, under the same namespace. For further information see [Working with the Gradle registry](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-gradle-registry).

### Provide your repository credentials

Your credentials to the GitHub Packages repository need to be defined in your local `gradle.properties` file, which you can find in your home directory, e.g. `~/.gradle/gradle.properties`.

Before doing this, ensure you have [created a personal access token](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/managing-your-personal-access-tokens#creating-a-personal-access-token-classic)
in GitHub and configured it with `repo`, `write:packages` and `read:packages` access. The token must also be [authorized with (MoJ) SSO](https://docs.github.com/en/enterprise-cloud@latest/authentication/authenticating-with-saml-single-sign-on/authorizing-a-personal-access-token-for-use-with-saml-single-sign-on).

Once you have your personal access token, please add the following parameters to `~/.gradle/gradle.properties`:

```yaml
project.ext.gitPackageUser = <your GitHub username>
project.ext.gitPackageKey = <your GitHub access token>
```

Do not include `'` or `"` around your username or token as these are treated literally as part of the value by gradle.

### Applying the Plugin

In your (root) `build.gradle`, add the plugin dependency via the Gradle Plugin DSL, e.g:

```groovy
plugins {
    id 'uk.gov.laa.ccms.springboot.laa-ccms-spring-boot-gradle-plugin' version '<LATEST>' apply false
}
```

Where `<LATEST>` is the latest **release** version found [here](https://github.com/orgs/ministryofjustice/packages?repo_name=laa-ccms-spring-boot-common).

If this is not a multi-project build, you can remove `apply false` to apply the plugin at the root level. Otherwise, in your subprojects where the plugin is required you will need to apply the plugin:

```groovy
apply plugin: 'uk.gov.laa.ccms.springboot.laa-ccms-spring-boot-gradle-plugin'
```

## Available Starters

- [Authentication](laa-ccms-spring-boot-starters/laa-ccms-spring-boot-starter-auth/README.md)
- _**[TODO]**_ Exception Handling
- _**[TODO]**_ Entity Converters
