[![Ministry of Justice Repository Compliance Badge](https://github-community.service.justice.gov.uk/repository-standards/api/laa-spring-boot-common/badge)](https://github-community.service.justice.gov.uk/repository-standards/laa-spring-boot-common)

# LAA Spring Boot Common

Provides 2 plugins that configure plugins and apply common build logic,
and a set of starters that provide individual pieces of common functionality.

## Branch compatibility

- `main`: Spring Boot 4.x / Jackson 3 line; use releases from this branch if your service is on Boot 4.
- `spring3`: Spring Boot 3.x / Jackson 2 line; pin plugin and starter versions from releases built off this branch if you have not upgraded yet.
- Avoid mixing artifacts across the two lines; pick the release line from Maven Central that matches your Spring Boot major version.

## Available Plugins

### `laa-java-gradle-plugin` for Java Projects

A general purpose Java plugin for LAA projects.

  - apply [Java](https://docs.gradle.org/current/userguide/java_plugin.html) plugin, and configure a Java toolchain.
  - apply [Jacoco](https://docs.gradle.org/current/userguide/jacoco_plugin.html) plugin, and configure sensible defaults.
  - apply [Versions](https://github.com/ben-manes/gradle-versions-plugin) plugin, and configure the recommended versioning strategy.
  - apply [Checkstyle](https://docs.gradle.org/current/userguide/checkstyle_plugin.html) plugin, and configure sensible defaults.
  - apply [Test Logger](https://github.com/radarsh/gradle-test-logger-plugin) plugin for better readability of test outputs.
  - apply [Maven Publish](https://docs.gradle.org/current/userguide/publishing_maven.html) plugin, and configure LAA repositories and credential resolution for local development and pipelines. For publishing, the repository name can be overridden by setting the `repositoryName` property in your `gradle.properties`. This is helpful when your repository name is different from your project name.
  - apply [Gradle Release](https://github.com/researchgate/gradle-release) plugin, and define a release tag format.

In addition to this an `integrationTest` gradle task will be provided, that will run tests under `src/main/integrationTest`. All test tasks will also output increased logging (standard streams and stack traces) to aid with debugging.

```groovy
plugins {
    id 'uk.gov.justice.service.laa.laa-java-gradle-plugin' version '<latest>'
}
```

### `laa-spring-boot-gradle-plugin` for Java + Spring Boot projects

A SpringBoot convention plugin for LAA projects. All of the above + SpringBoot dependency version recommendations to simplify dependency management and avoid compatibility issues within a project.

  - apply the [LAA Java Gradle](#laa-java-gradle-plugin-for-java-projects) plugin
  - apply the [SpringBoot](https://plugins.gradle.org/plugin/org.springframework.boot) plugin
  - apply the [Dependency Management](https://plugins.gradle.org/plugin/io.spring.dependency-management) plugin, and configure dependency management for the common LAA Spring Boot components (starters & libraries)

```groovy
plugins {
    id 'uk.gov.justice.service.laa.laa-spring-boot-gradle-plugin' version '<latest>'
}
```

### `laa-spring-boot-starter-export-codegen-gradle-plugin` for Export Code Generation

Optional plugin that registers CSV export code generation tasks used with the export starter.
Use this if you want both SQL and controller generation explicitly.

```groovy
plugins {
    id 'uk.gov.justice.service.laa.laa-spring-boot-starter-export-codegen-gradle-plugin' version '<latest>'
}
```

`laa-spring-boot-gradle-plugin` auto-registers export code generation when
`laa-spring-boot-starter-export` is on the dependency graph. It registers both SQL and controller tasks by default.

## Using the Plugins

For the plugins to work in your project, configure Maven Central as a plugin repository.

### Define the Plugin repository

To configure the plugin repository, add this **to the top** your project's `settings.gradle`:

```groovy
pluginManagement {
    repositories {
        maven {
            url = uri('https://central.sonatype.com/repository/maven-snapshots/')
            mavenContent {
                snapshotsOnly()
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
```

The Sonatype snapshots repository is only required for snapshot versions. Releases resolve from Maven Central.

### Applying the Plugin

In your (root) `build.gradle`, add the plugin dependency via the Gradle Plugin DSL, e.g:

```groovy
plugins {
    id 'uk.gov.justice.service.laa.laa-spring-boot-gradle-plugin' version '<LATEST>' apply false
}
```

Where `<LATEST>` is the latest **release** version published for `uk.gov.justice.service.laa:laa-spring-boot-gradle-plugin`.

If this is not a multi-project build, you can remove `apply false` to apply the plugin at the root level. Otherwise, in your subprojects where the plugin is required you will need to apply the plugin:

```groovy
apply plugin: 'uk.gov.justice.service.laa.laa-spring-boot-gradle-plugin'
```

## Available Starters

- [Gov-uk custom thymeleaf dialect](laa-spring-boot-starters/laa-spring-boot-starter-govuk-dialect)

- [Authentication](laa-spring-boot-starters/laa-spring-boot-starter-auth/README.md)
- [OAuth2 resource server auth](laa-spring-boot-starters/laa-spring-boot-starter-oauth2/README.md)
- [Application exception handling](laa-spring-boot-starters/laa-spring-boot-starter-application-exception/README.md)
  (deprecated – prefer Spring's built-in `ProblemDetail`/`ErrorResponse` RFC 9457 support)
- [Slack alerts](laa-spring-boot-starters/laa-spring-boot-starter-slack-alerts/README.md)
- [SQL input scanning](laa-spring-boot-starters/laa-spring-boot-starter-sql-scanner/README.md)
- [CSV exports](laa-spring-boot-starters/laa-spring-boot-starter-export/README.md)

## Contributing
Follow the [contribution guide](./CONTRIBUTING.md) to make code changes.

## Downstream bump automation

This repository includes a workflow that opens downstream PRs to bump:
`uk.gov.justice.service.laa.laa-spring-boot-gradle-plugin`

The update step scans Gradle build files recursively in the downstream repository,
including nested module paths such as `some-module/build.gradle` and Kotlin DSL variants (`*.gradle.kts`).

Workflow file:
- `.github/workflows/bump-downstream-on-release.yml`

### Triggers

- `release.created`: runs automatically when a release is created.
- `workflow_dispatch`: allows manual runs from the Actions tab.

### Required repository/org settings

- Repository (or org) variable: `downstream_repos` (or `DOWNSTREAM_REPOS`)
  - Comma-separated list of repositories to update.
  - You can use short names (for example `laa-spring-boot-microservice-template`) or full names (`owner/repo`).
  - If owner is omitted, `ministryofjustice/` is automatically prefixed.

Example:

```text
laa-spring-boot-microservice-template,laa-another-service,ministryofjustice/some-other-repo
```

### Workflow dispatch inputs

- `new_version` (required): version to bump to, for example `2.1.7` or `v2.1.7`.
- `downstream_repos` (optional): comma-separated override for this run only.
  - If omitted, the workflow uses `downstream_repos` / `DOWNSTREAM_REPOS` variable.

### Release version behavior

For release-triggered runs, `new_version` is taken from the release tag (`github.event.release.tag_name`), a leading `v` is removed, and a leading `<repository-name>-` prefix is also removed when present.

Examples:
- `v2.1.7` -> `2.1.7`
- `2.1.7` -> `2.1.7`
- `laa-spring-boot-common-2.1.7` -> `2.1.7`
