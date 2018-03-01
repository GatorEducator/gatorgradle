package org.gatorgrader;

import org.gatorgrader.internal.Dependency;
import org.gatorgrader.internal.DependencyManager;

import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GatorGraderPlugin implements Plugin<Project> {
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

        if (!DependencyManager.installOrUpdate(Dependency.GATORGRADER)) {
            throw new RuntimeException("Failed to install gatorgrader!");
        }
    }

    /**
     * Applies the GatorGrader plugin to the given project.
     *
     * @param project the project to apply GatorGrader to
     */
    public void apply(Project project) {
        Task grade = project.getTasks().create("grade", DefaultTask.class);

        // TODO: locate config file
        String configFileLocation = "config/gatorgrader.yml";
        File configFile           = new File(configFileLocation);
        GatorGraderConfig config  = new GatorGraderConfig(configFile);

        for (Command cmd : config) {
            // grade.doLast(new Action<Task>() {
            //     public void execute(Task task) {
            //         cmd.execute(true);
            //     }
            // });
        }

        // ensure dependencies are run sequentially if scheduled at the same time
        // this is probably done elsewhere as well, but might as well be sure
        project.getTasks().getByName("build").mustRunAfter("clean");
    }

    private Iterable<Command> compileGatorGraderCalls() {
        List<Command> cmds = new ArrayList<>();

        cmds.add(new Command("./gatorgrader.sh").with("--check").outputToSysOut(true));

        // for (int i = 0; i < 100; i++) {
        //     cmds.add(new Command("echo").with("" + i).outputToSysOut(true));
        //     cmds.add(new Command("sleep").with("0.33"));
        // }

        return cmds;
    }
}
