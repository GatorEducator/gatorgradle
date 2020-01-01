package org.gatorgradle.task;

import org.gatorgradle.task.GatorGradleTask;
import org.gatorgradle.util.StringUtil;

import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;

public class GatorGradleReportTask extends GatorGradleTask {

  /**
   * Upload the check report executed by the dependOn GatorGradleGradeTask.
   */
  @TaskAction
  public void report() {
    if (summary == null) {
      throw new GradleException(StringUtil.color(StringUtil.BAD, "Grading not run -- try gradle --continue grade report"));
    }

    summary.uploadOutputSummary();
  }
}
