package org.gatorgradle.task;

import org.gatorgradle.task.GatorGradleTask;

import org.gradle.api.tasks.TaskAction;

public class GatorGradleReportTask extends GatorGradleTask {

  /**
   * Upload the check report executed by the dependOn GatorGradleGradeTask.
   */
  @TaskAction
  public void report() {
    summary.uploadOutputSummary();
  }
}
