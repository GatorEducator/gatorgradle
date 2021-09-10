package org.gatorgradle.task;

import java.io.File;

import org.gatorgradle.command.BasicCommand;
import org.gatorgradle.command.Command;
import org.gatorgradle.config.GatorGradleConfig;
import org.gatorgradle.display.CommandOutputSummary;
import org.gatorgradle.internal.DependencyManager;
import org.gatorgradle.util.Console;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;

public abstract class GatorGradleTask extends DefaultTask {

  protected GatorGradleConfig config;
  protected File workingDir;

  static CommandOutputSummary summary;

  public void setConfig(GatorGradleConfig config) {
    this.config = config;
  }

  /** Dependency management. */
  protected void act() {
    getConfig().parseHeader();

    // ensure GatorGrader and dependencies are installed
    String dep = DependencyManager.manage();
    if (!dep.isEmpty()) {
      throw new GradleException(dep);
    }

    Console.newline(1);

    if (getConfig().hasStartupCommand()) {
      Console.log("Starting up...");
      BasicCommand startup = (BasicCommand) getConfig().getStartupCommand();
      startup.outputToSysOut(true);
      startup.run();
      if (startup.exitValue() != Command.SUCCESS) {
        throw new GradleException(
            "Startup command '" + startup + "' failed with exit code " + startup.exitValue() + "!");
      }
      Console.log("Ready!");
    }

    Console.newline(2);

    getConfig().parseBody();
  }

  /**
   * Ensure we got a configuration.
   *
   * @return configuration
   */
  @Input
  public GatorGradleConfig getConfig() {
    if (config == null) {
      throw new GradleException(
          "GatorGradle grade task's configuration was not specified correctly!");
    } else {
      return config;
    }
  }

  public void setWorkingDir(File dir) {
    this.workingDir = dir;
  }

  @InputDirectory
  public File getWorkingDir() {
    return workingDir;
  }
}
