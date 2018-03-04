package org.gatorgradle.task;

import static org.gatorgradle.GatorGradlePlugin.CONFIG_FILE_LOCATION;

import org.gatorgradle.GatorGradlePlugin;
import org.gatorgradle.command.BasicCommand;
import org.gatorgradle.command.Command;
import org.gatorgradle.command.GatorGraderCommand;
import org.gatorgradle.config.GatorGradleConfig;
import org.gatorgradle.internal.Dependency;
import org.gatorgradle.internal.DependencyManager;
import org.gatorgradle.internal.ProgressLoggerWrapper;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.workers.IsolationMode;
import org.gradle.workers.WorkerExecutor;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
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

    // because of Java serialization limitations, along with
    // how gradle implements logging, these must be static
    private static int numTasksCompleted;
    private static int totalTasks;
    private static int percentComplete;

    public static synchronized void completedTask() {
        numTasksCompleted += 1;
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

        // get a progress logger
        ProgressLoggerWrapper progLog = new ProgressLoggerWrapper(super.getProject(), "Graded");

        // start task submission
        progLog.started();
        totalTasks        = config.size();
        numTasksCompleted = 0;
        percentComplete   = 0;
        // submit commands to executor
        for (Command cmd : config) {
            // configure command
            cmd.setCallback((Runnable & Serializable) GatorGradleTask::completedTask);
            if (cmd.getWorkingDir() == null) {
                cmd.setWorkingDir(workingDir);
            }

            // configure command executor
            executor.submit(CommandExecutor.class, (conf) -> {
                conf.setIsolationMode(IsolationMode.NONE);
                conf.setDisplayName(cmd.getDescription());
                conf.setParams(cmd);
            });
        }

        while (totalTasks > numTasksCompleted) {
            percentComplete = (numTasksCompleted * 100) / totalTasks;
            progLog.progress("Finished " + (numTasksCompleted) + " / " + totalTasks
                             + " checks  --  " + percentComplete + "% complete!");
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                System.err.println("Failed to sleep");
            }
        }

        // make sure tasks have ended
        executor.await();

        for (Command cmd : config) {
            System.out.println("COMMAND " + cmd.getDescription());
            System.out.println("EXIT VALUE: " + cmd.exitValue());
            if (cmd instanceof BasicCommand) {
                System.out.println("OUTPUT: " + ((BasicCommand) cmd).getOutput());
            }
        }

        // complete task submission
        progLog.completed();
    }
}
