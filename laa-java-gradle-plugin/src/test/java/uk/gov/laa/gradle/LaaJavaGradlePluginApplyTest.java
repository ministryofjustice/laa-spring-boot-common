package uk.gov.laa.gradle;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

public class LaaJavaGradlePluginApplyTest {

    @Test
    public void testApplyLaaJavaGradlePlugin() {
        LaaJavaGradlePlugin plugin = new LaaJavaGradlePlugin();
        Project project = ProjectBuilder.builder().build();
        plugin.apply(project);
    }
}
