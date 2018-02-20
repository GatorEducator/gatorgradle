package org.gatorgrader;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class GatorGraderPlugin implements Plugin<Project> {
    public void apply(Project project) {
        GatorGraderTask grade = project.getTasks().create("grade", GatorGraderTask.class);

        // ensure dependencies are run sequentially if scheduled at the same time
        project.getTasks().getByName("build").mustRunAfter("clean");
    }
}
