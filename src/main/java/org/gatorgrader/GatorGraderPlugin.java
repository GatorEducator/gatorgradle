package org.gatorgrader;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class GatorGraderPlugin implements Plugin<Project> {
    public void apply(Project project) {
        GradeTask grade = project.getTasks().create("grade", GradeTask.class);
        grade.dependsOn("clean").dependsOn("check").dependsOn("build");
    }
}
