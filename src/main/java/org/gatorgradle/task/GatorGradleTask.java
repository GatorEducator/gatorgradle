package org.gatorgradle.task;

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

import org.gatorgradle.GatorGradlePlugin;
import org.gatorgradle.command.BasicCommand;
import org.gatorgradle.command.Command;
import org.gatorgradle.command.GatorGraderCommand;
import org.gatorgradle.config.GatorGradleConfig;
import org.gatorgradle.display.CommandOutputSummary;
import org.gatorgradle.internal.Dependency;
import org.gatorgradle.internal.DependencyManager;
import org.gatorgradle.internal.ProgressLoggerWrapper;
import org.gatorgradle.util.Console;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.logging.Logger;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.workers.IsolationMode;
import org.gradle.workers.WorkerExecutor;

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
    // Console.log("FINISHED " + complete.toString());

    // To break the build if wanted, throw a GradleException here
    // throw new GradleException(this);
  }

  private static synchronized void initTasks(int total, Logger logger) {
    totalTasks = total;
    summary = new CommandOutputSummary(logger);
  }

  /**
   * Execute the grading checks assigned to this GatorGradleTask.
   */
  @TaskAction
  public void grade() {
    // ensure GatorGrader and dependencies are installed
    for (Dependency dep : Dependency.values()) {
      if (!DependencyManager.installOrUpdate(dep)) {
        throw new GradleException(dep.name() + " not installed!");
      }
    }

    // parse config after dependencies managed
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
          conf.setDisplayName(cmd.toString());
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
            + summary.getNumCompletedTasks() + " tasks but should have completed " + totalTasks);
      }
    }

    progLog.progress("Finished " + summary.getNumCompletedTasks() + " / " + totalTasks
        + " checks  >  100% complete!  >  Compiling Report...");

    // complete task submission
    progLog.completed();

    summary.showOutputSummary();
  }
}
