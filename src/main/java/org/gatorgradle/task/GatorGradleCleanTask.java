package org.gatorgradle.task;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.gatorgradle.GatorGradlePlugin;
import org.gatorgradle.util.Console;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;

public class GatorGradleCleanTask extends DefaultTask {

  @TaskAction
  public void clean() {
    Path gatorgrader = Paths.get(GatorGradlePlugin.GATORGRADER_HOME).toAbsolutePath();
    if (Files.exists(gatorgrader)) {
      try {
        Files.delete(gatorgrader);
      } catch (IOException ex) {
        throw new GradleException("Failed to delete " + gatorgrader, ex);
      }
    } else {
      Console.log("GatorGradle is not installed");
    }
  }
}
