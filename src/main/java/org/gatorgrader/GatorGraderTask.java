package org.gatorgrader;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.util.HashMap;
import java.util.Map;

public class GatorGraderTask extends DefaultTask {
    public static final String NO_VALUE = "$SPECIAL_TOKEN_NO_VALUE$";

    protected Map<String, String> grading;

    protected boolean showWelcomeMessage = false;

    /**
     * Create the default, empty GatorGraderTask.
     */
    public GatorGraderTask() {
        grading = new HashMap<>();
    }

    /**
     * Create a GatorGraderTask with the given checks and values.
     *
     * @param grading the map of checks to values
     */
    public GatorGraderTask(Map<String, String> grading) {
        this.grading = new HashMap<>(grading);
    }

    /**
     * Create a GatorGraderTask based on another GatorGraderTask.
     *
     * @param derivedFrom the GatorGraderTask to take checks and values from
     */
    public GatorGraderTask(GatorGraderTask derivedFrom) {
        this(derivedFrom.grading);
    }

    /**
     * Add a new grading check to this GatorGraderTask with the given value.
     *
     * @param check the grading check to execute
     * @param value the argument for the check
     * @return this GatorGraderTask
     */
    public GatorGraderTask with(String check, String value) {
        grading.put(check, value);

        return this;
    }

    /**
     * Add a new grading check or flag to this GatorGraderTask with no value.
     *
     * @param check the grading check or flag to execute
     * @return this GatorGraderTask
     */
    public GatorGraderTask with(String check) {
        grading.put(check, NO_VALUE);

        return this;
    }

    public GatorGraderTask derive() {
        return new GatorGraderTask(this);
    }

    /**
     * Execute the grading checks assigned to this GatorGraderTask.
     */
    @TaskAction
    public void grade() {
        Command com = new Command("python3").with("gatorgrader/gatorgrader.py");
        if (!showWelcomeMessage) {
            com.with("--nowelcome");
        }
        for (String check : grading.keySet()) {
            com.with("--" + check);
            if (!NO_VALUE.equals(grading.get(check))) {
                com.with(grading.get(check));
            }
        }
    }
}
