package org.gatorgrader;

import org.gradle.api.DefaultTask;

public class GradeTask extends DefaultTask {
    /**
     * Create the default, empty GradeTask.
     */
    public GradeTask() {
        super.dependsOn("build");
        super.dependsOn("check");
        super.dependsOn("clean");
    }
}
