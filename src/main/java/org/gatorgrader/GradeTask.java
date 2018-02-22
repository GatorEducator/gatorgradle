package org.gatorgrader;

import org.gradle.api.DefaultTask;

import java.util.HashMap;
import java.util.Map;

public class GradeTask extends DefaultTask {
    Map<String, String> grading;

    /**
     * Create the default, empty GradeTask.
     */
    public GradeTask() {
        grading = new HashMap<>();
    }

    /**
     * Create a GradeTask with the given checks and values.
     *
     * @param grading the map of checks to values
     */
    public GradeTask(Map<String, String> grading) {
        this.grading = new HashMap<>(grading);
    }

    /**
     * Create a GradeTask based on another GradeTask.
     *
     * @param derivedFrom the GradeTask to take checks and values from
     */
    public GradeTask(GradeTask derivedFrom) {
        this.grading = new HashMap<>(grading);
    }

    /**
     * Add a new grading check to this GradeTask with the given value.
     *
     * @param check the grading check to execute
     * @param value the argument for the check
     * @return this GradeTask
     */
    public GradeTask with(String check, String value) {
        grading.put(check, value);

        return this;
    }

    public GradeTask derive() {
        return new GradeTask(this);
    }
}
