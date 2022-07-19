package org.gatorgradle.task;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

import org.gatorgradle.GatorGradlePlugin;
import org.gatorgradle.command.BasicCommand;
import org.gatorgradle.util.Console;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;

public class GatorGradleCleanTask extends DefaultTask {

  @TaskAction
  public void clean() {
    deleteGatorGraderInstallation();
  }

  public static void deleteGatorGraderInstallation() {
    Path gatorgrader = Paths.get(GatorGradlePlugin.GATORGRADER_HOME).toAbsolutePath();
    if (Files.exists(gatorgrader)) {
      try {
        BasicCommand dep = new BasicCommand("pipenv", "--rm");
        dep.setWorkingDir(gatorgrader.toFile());
        dep.outputToSysOut(false);
        dep.run(true);
      } catch (Throwable ex) {
        throw new GradleException("Failed to remove pipenv environment " + gatorgrader, ex);
      }
      try {
        Files.walk(gatorgrader)
            .sorted(Comparator.reverseOrder())
            .map(innerPath -> innerPath.toFile())
            .forEach(
                file -> {
                  boolean deleted = file.delete();
                  if (!deleted) {
                    Console.error("Could not delete", file);
                  }
                });
      } catch (IOException ex) {
        throw new GradleException("Failed to delete " + gatorgrader, ex);
      }
    } else {
      Console.log("GatorGradle is not installed");
    }
  }
}
