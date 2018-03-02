package org.gatorgradle.task;

import static org.gatorgradle.GatorGradlePlugin.CONFIG_FILE_LOCATION;

import org.gatorgradle.GatorGradlePlugin;
import org.gatorgradle.command.Command;
import org.gatorgradle.command.GatorGraderCommand;
import org.gatorgradle.config.GatorGradleConfig;
import org.gatorgradle.internal.Dependency;
import org.gatorgradle.internal.DependencyManager;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.workers.IsolationMode;
import org.gradle.workers.WorkerExecutor;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

public class GatorGradleTask extends DefaultTask {
    // The executor to use to execute the grading
    private final WorkerExecutor executor;

    private GatorGradleConfig config;
    private File workingDir;

    @Inject
    public GatorGradleTask(WorkerExecutor executor) {
        this.executor = executor;
    }

    public void setConfig(GatorGradleConfig config) {
        this.config = config;
    }

    @Input
    public GatorGradleConfig getConfig() {
        return config;
    }

    public void setWorkingDir(File dir) {
        this.workingDir = dir;
    }

    @InputDirectory
    public File getWorkingDir() {
        return workingDir;
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

        // ensure we have a configuration
        if (config == null) {
            throw new RuntimeException(
                "GatorGradle grade task's configuration was not specified correctly!");
        }

        // submit commands to executor
        for (Command cmd : config) {
            executor.submit(CommandExecutor.class, (conf) -> {
                if (cmd.getWorkingDir() == null) {
                    cmd.setWorkingDir(workingDir);
                }
                conf.setIsolationMode(IsolationMode.NONE);
                conf.setDisplayName(cmd.getDescription());
                conf.setParams(cmd);
            });
        }
    }
}
