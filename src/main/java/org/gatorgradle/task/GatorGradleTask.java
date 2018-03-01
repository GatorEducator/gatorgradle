package org.gatorgradle.task;

import static org.gatorgradle.GatorGradlePlugin.CONFIG_FILE_LOCATION;

import org.gatorgradle.GatorGradlePlugin;
import org.gatorgradle.command.Command;
import org.gatorgradle.command.GatorGraderCommand;
import org.gatorgradle.config.GatorGradleConfig;
import org.gatorgradle.internal.Dependency;
import org.gatorgradle.internal.DependencyManager;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.workers.IsolationMode;
import org.gradle.workers.WorkerExecutor;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

public class GatorGradleTask extends DefaultTask {
    // The executor to use to execute the grading
    final WorkerExecutor executor;

    @Inject
    public GatorGradleTask(WorkerExecutor executor) {
        this.executor = executor;
    }

    /**
     * Execute the grading checks assigned to this GatorGradleTask.
     */
    @TaskAction
    public void grade() {
        // ensure GatorGrader is installed
        if (!DependencyManager.installOrUpdate(Dependency.GATORGRADER)) {
            throw new RuntimeException("Failed to install gatorgrader!");
        }

        System.out.println("getting configuration");

        // get commands to run with GatorGradleConfig
        GatorGradleConfig config = GatorGradlePlugin.getConfig();

        // submit commands to executor
        for (Command cmd : config) {
            executor.submit(CommandExecutor.class, (conf) -> {
                conf.setIsolationMode(IsolationMode.NONE);
                conf.setDisplayName(cmd.description());
                conf.setParams(cmd);
            });
        }
    }
}
