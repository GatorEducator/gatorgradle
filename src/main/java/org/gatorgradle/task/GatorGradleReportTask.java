package org.gatorgradle.task;

import org.gatorgradle.task.GatorGradleTask;
import org.gatorgradle.util.StringUtil;

import org.gradle.api.tasks.TaskAction;
import org.gradle.api.GradleException;

public class GatorGradleReportTask extends GatorGradleTask {

  /**
   * Upload the check report executed by the dependOn GatorGradleGradeTask.
   */
  @TaskAction
  public void report() {
    if (summary == null) {
      throw new GradleException(StringUtil.color(StringUtil.BAD, "No report made! Try gradle --continue grade report"));
    }

    summary.uploadOutputSummary();
  }
}
