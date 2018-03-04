package org.gatorgradle;

import org.gatorgradle.config.GatorGradleConfig;
import org.gatorgradle.task.GatorGradleTask;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;

import java.io.File;

/**
 * GatorGradlePlugin applies the plugin to a project, registers
 * the grade task, and sets up some sensible defaults.
 * TODO: allow DSL configuration block to specify
 *        CONFIG_FILE_LOCATION and GATORGRADER_HOME.
 */
public class GatorGradlePlugin implements Plugin<Project> {
    public static final String GATORGRADER_HOME;
    public static final String CONFIG_FILE_LOCATION;
    public static final String USER_HOME;
    public static final String F_SEP;
    public static final String OS;

    public static Logger logger;

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

        // TODO: is this a sensible default for gg home? - probably only on linux
        if (OS.equals("linux")) {
            GATORGRADER_HOME =
                USER_HOME + F_SEP + ".local" + F_SEP + "share" + F_SEP + "gatorgrader";
        } else {
            GATORGRADER_HOME = USER_HOME + F_SEP + ".gatorgrader";
        }

        CONFIG_FILE_LOCATION = "config/gatorgrader.yml";
    }

    /**
     * Applies the GatorGrader plugin to the given project.
     *
     * @param project the project to apply GatorGrader to
     */
    public void apply(Project project) {
        logger = project.getLogger();
        // set config file location, then generate config
        // TODO: what should we do for config file location?
        File conFile = project.file(CONFIG_FILE_LOCATION);

        GatorGradleConfig config = new GatorGradleConfig(conFile);

        // create gatorgradle 'grade' task
        GatorGradleTask grade = project.getTasks().create("grade", GatorGradleTask.class, task -> {
            // default grade task uses config from above and project dir as grade
            task.setConfig(config);
            task.setWorkingDir(project.getProjectDir());
        });

        // ensure dependencies are run sequentially if scheduled at the same time
        // this is probably done elsewhere as well, but might as well be sure
        project.getTasks().getByName("build").mustRunAfter("clean");
        project.getTasks().getByName("grade").mustRunAfter("build");
    }
}
