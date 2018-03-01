package org.gatorgradle;

import org.gatorgradle.command.Command;
import org.gatorgradle.internal.Dependency;
import org.gatorgradle.internal.DependencyManager;

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
    }

    /**
     * Applies the GatorGrader plugin to the given project.
     *
     * @param project the project to apply GatorGrader to
     */
    public void apply(Project project) {
        GatorGradleTask grade = project.getTasks().create("grade", GatorGradleTask.class);
        addDynamicTasks(grade);

        // ensure dependencies are run sequentially if scheduled at the same time
        // this is probably done elsewhere as well, but might as well be sure
        project.getTasks().getByName("build").mustRunAfter("clean");
    }

    private void addDynamicTasks(GatorGradleTask gradeTask) {
        // TODO: locate config file
        String configFileLocation = "config/gatorgrader.yml";
        File configFile           = new File(configFileLocation);
        GatorGradleConfig config  = new GatorGradleConfig(configFile);

        System.out.println("Building grade task!");
        System.out.println(config);

        gradeTask.doFirst((task) -> {
            System.out.println("test");
            if (!DependencyManager.installOrUpdate(Dependency.GATORGRADER)) {
                throw new RuntimeException("Failed to install gatorgrader!");
            }
        });

        for (Command cmd : config) {
            System.out.println(cmd);
            gradeTask.doLast((task) -> cmd.run(true));
        }
        // return task;
    }
}
