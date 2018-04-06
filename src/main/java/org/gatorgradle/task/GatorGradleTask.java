package org.gatorgradle.task;

import org.gatorgradle.GatorGradlePlugin;
import org.gatorgradle.command.*;
import org.gatorgradle.config.GatorGradleConfig;
import org.gatorgradle.display.CommandOutputSummary;
import org.gatorgradle.internal.*;
import org.gatorgradle.util.Console;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.logging.Logger;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

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
    private static int totalTasks;
    private static CommandOutputSummary summary;

    /**
     * Static handler to call when a subtask completes.
     *
     * @param complete the command that was run
     */
    private static synchronized void completedTask(Command complete) {
        summary.addCompletedCommand(complete);
        // Console.log("FINISHED " + complete.getDescription());

        // To break the build if wanted, throw a GradleException here
        // throw new GradleException(this);
    }

    private static synchronized void initTasks(int total, Logger logger) {
        totalTasks = total;
        summary    = new CommandOutputSummary(logger);
    }

    /**
     * Execute the grading checks assigned to this GatorGradleTask.
     */
    @TaskAction
    public void grade() {
        // ensure GatorGrader and dependencies are installed
        if (!DependencyManager.installOrUpdate(Dependency.GIT)) {
            throw new GradleException("Git not installed!");
        }
        if (!DependencyManager.installOrUpdate(Dependency.PYTHON)) {
            throw new GradleException("Python not installed!");
        }
        if (!DependencyManager.installOrUpdate(Dependency.GATORGRADER)) {
            throw new GradleException("GatorGrader not installed!");
        }

        // ensure we have a configuration
        if (config == null) {
            throw new GradleException(
                "GatorGradle grade task's configuration was not specified correctly!");
        }

        config.parse();

        // get a progress logger
        ProgressLoggerWrapper progLog =
            new ProgressLoggerWrapper(super.getProject(), config.getAssignmentName());

        // start task submission
        progLog.started();
        initTasks(config.size(), this.getLogger());

        if (totalTasks > 0) {
            // submit commands to executor
            for (Command cmd : config) {
                // configure command
                cmd.setCallback((Command.Callback) GatorGradleTask::completedTask);
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

            int percentComplete = 0;
            while (percentComplete < 100) {
                percentComplete = (summary.getNumCompletedTasks() * 100) / totalTasks;
                progLog.progress("Finished " + summary.getNumCompletedTasks() + " / " + totalTasks
                                 + " checks  >  " + percentComplete + "% complete!");
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    Console.error("Failed to sleep");
                }
            }

            // make sure tasks have ended
            executor.await();

            // this is impossible now because of the for loop above, FIXME
            if (summary.getNumCompletedTasks() != totalTasks) {
                // silent failure somewhere, break the build
                throw new GradleException("Silent failure in task execution! Only completed "
                                          + summary.getNumCompletedTasks()
                                          + " tasks but should have completed " + totalTasks);
            }
        }

        progLog.progress("Finished " + summary.getNumCompletedTasks() + " / " + totalTasks
                         + " checks  >  100% complete!  >  Compiling Report...");

        summary.showOutputSummary();

        // complete task submission
        progLog.completed();
    }
}
