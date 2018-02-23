package org.gatorgrader;

import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GatorGraderPlugin implements Plugin<Project> {
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
