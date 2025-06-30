package uk.gov.laa.ccms.gradle

import com.adarshr.gradle.testlogger.TestLoggerPlugin
import com.github.benmanes.gradle.versions.VersionsPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.quality.CheckstylePlugin
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.api.publish.maven.tasks.AbstractPublishToMaven
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.testing.jacoco.plugins.JacocoPlugin

class LaaCcmsJavaGradlePlugin implements Plugin<Project> {

    private static final String JAVA_VERSION = "21"
    private static final String CHECKSTYLE_VERSION = "10.26.0"

    @Override
    void apply(Project target) {

        target.pluginManager.apply JavaPlugin
        target.pluginManager.apply JacocoPlugin
        target.pluginManager.apply VersionsPlugin
        target.pluginManager.apply CheckstylePlugin
        target.pluginManager.apply TestLoggerPlugin
        target.pluginManager.apply MavenPublishPlugin

        target.java {
            toolchain.languageVersion.set(JavaLanguageVersion.of(JAVA_VERSION))
        }

        /** Test Tasks **/

        // Integration tests
        if (target.tasks.findByName('integrationTest') == null) {
            target.sourceSets {
                integrationTest {
                    java {
                        compileClasspath += main.output + test.output
                        runtimeClasspath += main.output + test.output
                    }
                }
            }

            target.configurations {
                compileOnly {
                    extendsFrom annotationProcessor
                }
                integrationTestImplementation.extendsFrom(testImplementation)
                integrationTestRuntimeOnly.extendsFrom(testRuntimeOnly)
            }

            target.tasks.register('integrationTest', Test) {
                useJUnitPlatform()
                description = "Run integration tests"
                group = "verification"
                testClassesDirs = target.sourceSets.integrationTest.output.classesDirs
                classpath = target.sourceSets.integrationTest.runtimeClasspath
            }
        }

        /** Code checking **/

        target.dependencyUpdates {
            def isReleaseVersion = { String version ->
                def stableKeyword = ['RELEASE', 'FINAL', 'GA'].any { kw -> version.toUpperCase().contains(kw) }
                def regex = /^[0-9,.v-]+(-r)?$/
                return stableKeyword || version ==~ regex
            }

            rejectVersionIf {
                !isReleaseVersion(it.candidate.version) && isReleaseVersion(it.currentVersion)
            }

            gradleReleaseChannel = "current"
        }

        target.jacocoTestReport {
            sourceDirectories.from target.files('build/generated/sources/annotationProcessor/java/main')

            dependsOn target.tasks['test']
        }

        target.jacocoTestCoverageVerification {

            violationRules {
                rule {
                    limit {
                        minimum = 0.80
                    }
                }
            }
        }

        target.checkstyle {
            maxWarnings = 0
            toolVersion = CHECKSTYLE_VERSION
            sourceSets = [sourceSets.main]
            showViolations = true
        }

        /** Package repositories **/

        def gitHubPackagesUsername
        def gitHubPackagesPassword

        def envUsername = System.getenv("GITHUB_ACTOR")?.trim()
        def envKey = System.getenv("GITHUB_TOKEN")?.trim()

        def propertyUsername = target.findProperty('project.ext.gitPackageUser')
        def propertyKey = target.findProperty('project.ext.gitPackageKey')

        if (envUsername && envKey) {
            gitHubPackagesUsername = envUsername
            gitHubPackagesPassword = envKey
        } else if (propertyUsername && propertyKey) {
            gitHubPackagesUsername = propertyUsername
            gitHubPackagesPassword = propertyKey
        } else {
            target.logger.info("Unable to find GitHub packages credentials. " +
                    "Please set 'project.ext.gitPackageUser' / 'project.ext.gitPackageKey' " +
                    "in your gradle.properties file " +
                    "or 'GITHUB_ACTOR' / 'GITHUB_TOKEN' environment variables.")
        }

        target.project.ext."gitHubPackagesUsername" = gitHubPackagesUsername
        target.project.ext."gitHubPackagesPassword" = gitHubPackagesPassword

        target.repositories {
            mavenCentral()
            maven {
                url 'https://maven.pkg.github.com/ministryofjustice/laa-ccms-spring-boot-common'
                credentials {
                    username = target.gitHubPackagesUsername
                    password = target.gitHubPackagesPassword
                }
            }
        }

        /** Publishing and releases **/

        // Allow override through gradle property, in case project name ≠ repository name
        def repositoryName = target.findProperty('repositoryName')?.trim() ?: target.rootProject.name

        target.publishing {
            repositories {
                maven {
                    url "https://maven.pkg.github.com/ministryofjustice/${repositoryName}"
                    credentials {
                        username = target.gitHubPackagesUsername
                        password = target.gitHubPackagesPassword
                    }
                }
            }
        }

        target.tasks.withType(AbstractPublishToMaven).configureEach {
            doLast {
                logger.lifecycle("Published Maven artifact: " +
                        "${publication.groupId}:${publication.artifactId}:${publication.version}")
            }
        }

        // Used for deploying snapshot packages
        if (target.rootProject.tasks.findByName('updateSnapshotVersion') == null) {
            target.rootProject.tasks.register("updateSnapshotVersion") {
                doLast(task -> {
                    def gitHash = "git rev-parse --short HEAD".execute().text.trim()
                    def propertiesFile = target.rootProject.file('gradle.properties')
                    def properties = new Properties()
                    properties.load(new FileInputStream(propertiesFile))

                    def currentVersion = properties.getProperty('version')
                    def newVersion = currentVersion.replace('-SNAPSHOT', "-${gitHash}-SNAPSHOT")
                    properties.setProperty('version', newVersion)
                    properties.store(propertiesFile.newWriter(), null)
                }
                )
            }
        }
    }
}
