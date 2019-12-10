package org.gatorgradle.task;

import org.gatorgradle.task.GatorGradleTask;
import org.gatorgradle.command.BasicCommand;
import org.gatorgradle.command.Command;
import org.gatorgradle.config.GatorGradleConfig;
import org.gatorgradle.display.CommandOutputSummary;
import org.gatorgradle.internal.DependencyManager;
import org.gatorgradle.internal.ProgressLoggerWrapper;
import org.gatorgradle.util.Console;

import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;
import org.gradle.workers.IsolationMode;

public class GatorGradleReportTask extends GatorGradleTask {

  /**
   * Upload the check report executed by the dependOn GatorGradleGradeTask.
   */
  @TaskAction
  public void report() {
    summary.uploadOutputSummary();
  }
}
