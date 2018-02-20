package org.gatorgrader;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

public class GatorGraderTask extends DefaultTask {
    private String args;

    /**
     * Runs GatorGrader with specific arguments.
     */
    public GatorGraderTask() {
        super.dependsOn("build");
        super.dependsOn("check");
        super.dependsOn("clean");
    }

    public String getArguments() {
        return args;
    }

    public void setArguments(String args) {
        this.args = args;
    }

    @TaskAction
    public void executeTask() {
        System.out.println("Hello, World! Doing grading...");
    }
}
