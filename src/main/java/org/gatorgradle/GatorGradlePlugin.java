package org.gatorgradle;

import org.gatorgradle.command.Command;
import org.gatorgradle.config.GatorGradleConfig;
import org.gatorgradle.internal.Dependency;
import org.gatorgradle.internal.DependencyManager;
import org.gatorgradle.task.GatorGradleTask;

import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GatorGradlePlugin implements Plugin<Project> {
    public static String GATORGRADER_HOME;
    public static String CONFIG_FILE_LOCATION;
    public static final String USER_HOME;
    public static final String F_SEP;
    public static final String OS;

    static {
        F_SEP     = System.getProperty("file.separator");
        USER_HOME = System.getProperty("user.home");

        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("linux")) {
            OS = "linux";
        } else if (os.contains("windows")) {
            OS = "windows";
        } else if (os.contains("mac")) {
            OS = "mac";
        } else {
            OS = "unsupported";
        }

        // TODO: is this a sensible default for gg home?
        GATORGRADER_HOME = USER_HOME + F_SEP + ".gatorgrader";

        // TODO: is this a sensible default for config location?
        CONFIG_FILE_LOCATION = "config/gatorgrader.yml";
    }

    private static GatorGradleConfig config;

    /**
     * Applies the GatorGrader plugin to the given project.
     *
     * @param project the project to apply GatorGrader to
     */
    public void apply(Project project) {
        // set config file location, then generate config
        // TODO: what should we do for config file location?
        config = new GatorGradleConfig(new File(CONFIG_FILE_LOCATION));

        // create gatorgradle 'grade' task
        GatorGradleTask grade = project.getTasks().create("grade", GatorGradleTask.class);

        System.out.println("applied");

        // ensure dependencies are run sequentially if scheduled at the same time
        // this is probably done elsewhere as well, but might as well be sure
        project.getTasks().getByName("build").mustRunAfter("clean");
        project.getTasks().getByName("grade").mustRunAfter("build");
    }

    /**
     * Get the current GatorGradleConfig.
     *
     * @return the current configuration
     */
    public static GatorGradleConfig getConfig() {
        return config;
    }
}
