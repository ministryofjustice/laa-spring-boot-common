package uk.gov.laa.ccms.gradle;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

public class LaaCcmsJavaGradlePluginApplyTest {

    @Test
    public void testApplyLaaCcmsJavaGradlePlugin() {
        LaaCcmsJavaGradlePlugin plugin = new LaaCcmsJavaGradlePlugin();
        Project project = ProjectBuilder.builder().build();
        plugin.apply(project);
    }
}
