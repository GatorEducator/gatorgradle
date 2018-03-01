package org.gatorgradle;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.util.HashMap;
import java.util.Map;

public class GatorGradleTask extends DefaultTask {
    public static final String NO_VALUE = "$SPECIAL_TOKEN_NO_VALUE$";

    protected Map<String, String> grading;

    protected boolean showWelcomeMessage = false;

    /**
     * Create the default, empty GatorGradleTask.
     */
    public GatorGradleTask() {
        grading = new HashMap<>();
    }

    /**
     * Create a GatorGradleTask with the given checks and values.
     *
     * @param grading the map of checks to values
     */
    public GatorGradleTask(Map<String, String> grading) {
        this.grading = new HashMap<>(grading);
    }

    /**
     * Create a GatorGradleTask based on another GatorGradleTask.
     *
     * @param derivedFrom the GatorGradleTask to take checks and values from
     */
    public GatorGradleTask(GatorGradleTask derivedFrom) {
        this(derivedFrom.grading);
    }

    /**
     * Add a new grading check to this GatorGradleTask with the given value.
     *
     * @param check the grading check to execute
     * @param value the argument for the check
     * @return this GatorGradleTask
     */
    public GatorGradleTask with(String check, String value) {
        grading.put(check, value);

        return this;
    }

    /**
     * Add a new grading check or flag to this GatorGradleTask with no value.
     *
     * @param check the grading check or flag to execute
     * @return this GatorGradleTask
     */
    public GatorGradleTask with(String check) {
        grading.put(check, NO_VALUE);

        return this;
    }

    public GatorGradleTask derive() {
        return new GatorGradleTask(this);
    }

    /**
     * Execute the grading checks assigned to this GatorGradleTask.
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
