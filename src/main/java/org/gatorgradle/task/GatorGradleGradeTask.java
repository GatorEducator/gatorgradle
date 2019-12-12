package org.gatorgradle.task;

import javax.inject.Inject;

import org.gatorgradle.command.BasicCommand;
import org.gatorgradle.command.Command;
import org.gatorgradle.config.GatorGradleConfig;
import org.gatorgradle.display.CommandOutputSummary;
import org.gatorgradle.internal.DependencyManager;
import org.gatorgradle.internal.ProgressLoggerWrapper;
import org.gatorgradle.task.GatorGradleTask;
import org.gatorgradle.util.Console;

import org.gradle.api.GradleException;
import org.gradle.api.logging.Logger;
import org.gradle.api.tasks.TaskAction;
import org.gradle.workers.WorkerExecutor;
import org.gradle.workers.IsolationMode;

public class GatorGradleGradeTask extends GatorGradleTask {
  // The executor to use to execute the gradin
  private final WorkerExecutor executor;

  @Inject
  public GatorGradleGradeTask(WorkerExecutor executor) {
    this.executor = executor;
  }

  // because of Java serialization limitations, along with
  // how gradle implements logging, these must be static
  private static int totalTasks;


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
   * Execute the grading checks assigned to this GatorGradleGradeTask.
   */
  @TaskAction
  public void grade() {

    super.act();

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
        cmd.setCallback((Command.Callback) GatorGradleGradeTask::completedTask);
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
