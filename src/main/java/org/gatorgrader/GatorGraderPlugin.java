package org.gatorgrader;

import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;

import java.nio.file.Files;
import java.nio.file.Paths;
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

        // Temporary gatorgrader home for manual installation
        GATORGRADER_HOME = USER_HOME + F_SEP + ".gatorgrader";

        // quick git pull installation
        Command updateOrInstall = new Command().workingDir(USER_HOME);
        if (Files.exists(Paths.get(GATORGRADER_HOME))) {
            // gatorgrader repo exists (most likely)
            updateOrInstall.with("git", "pull");
        } else {
            updateOrInstall.with("git", "clone", "https://github.com/gkapfham/gatorgrader.git", GATORGRADER_HOME);
        }

        // install gatorgrader, and block until complete (FIXME: this needs to be better)
        updateOrInstall.run(true);
        if (updateOrInstall.exitValue() != Command.SUCCESS) {
            System.err.println("ERROR! updateOrInstall failed! Output:");
            System.err.println(updateOrInstall.getOutput());
        }
    }

    /**
     * Applies the GatorGrader plugin to the given project.
     *
     * @param project the project to apply GatorGrader to
     */
    public void apply(Project project) {
        Task grade = project.getTasks().create("grade", DefaultTask.class);

        Iterable<Command> commands = compileGatorGraderCalls();

        for (Command cmd : commands) {
            grade.doLast(new Action<Task>() {
                public void execute(Task task) {
                    cmd.execute(true);
                }
            });
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
