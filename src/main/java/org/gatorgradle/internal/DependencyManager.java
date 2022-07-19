package org.gatorgradle.internal;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.gatorgradle.GatorGradlePlugin;
import org.gatorgradle.command.BasicCommand;
import org.gatorgradle.command.Command;
import org.gatorgradle.config.GatorGradleConfig;
import org.gatorgradle.task.GatorGradleCleanTask;
import org.gatorgradle.util.Console;
import org.gradle.api.GradleException;

/**
 * Checks for, and (in the case of GatorGrader) downloads, installs, and configures dependencies.
 * The standard dependencies to handle are GatorGrader, Python 3, Pipenv, and Git.
 *
 * @author Saejin Mahlau-Heinert
 */
public class DependencyManager {
  public static final String GATORGRADER_GIT_REPO =
      "https://github.com/GatorEducator/gatorgrader.git";
  private static String PYTHON_EXECUTABLE = null;

  private static boolean managed = false;

  /**
   * Returns the python executable path.
   *
   * @return the path
   */
  public static String getPython() {
    if (!managed) {
      String dep = manage();
      if (!dep.isEmpty()) {
        throw new GradleException(dep);
      }
    }
    if (PYTHON_EXECUTABLE == null) {
      BasicCommand query = new BasicCommand("pipenv", "--venv");
      query.setWorkingDir(new File(GatorGradlePlugin.GATORGRADER_HOME));
      query.outputToSysOut(false);
      query.run(true);
      if (query.exitValue() != 0) {
        error("Query for the Python executable failed! Try to reinstall GatorGrader", query);
        throw new GradleException("Failed to run 'pipenv --venv'! Was GatorGrader installed?");
      }
      if (GatorGradlePlugin.OS.equals(GatorGradlePlugin.WINDOWS)) {
        PYTHON_EXECUTABLE =
            query.getOutput().trim()
                + GatorGradlePlugin.F_SEP
                + "Scripts"
                + GatorGradlePlugin.F_SEP
                + "python";
      } else {
        PYTHON_EXECUTABLE =
            query.getOutput().trim()
                + GatorGradlePlugin.F_SEP
                + "bin"
                + GatorGradlePlugin.F_SEP
                + "python";
      }
    }
    return PYTHON_EXECUTABLE;
  }

  /**
   * Manage dependencies.
   *
   * @return an empty String indicating success or a dependency name String indicating failure
   */
  public static String manage() {
    if (managed) {
      return "";
    }
    String error = "Git not installed!";
    if (!doGit()) {
      return error;
    }
    error = "Python not installed!";
    if (!doPython()) {
      return error;
    }
    error = "Pipenv not installed!";
    if (!doPipenv()) {
      return error;
    }
    error = "GatorGrader management failed; see above for details.";
    if (!doGatorGrader()) {
      return error;
    }
    managed = true;
    return "";
  }

  private static boolean doGit() {
    BasicCommand cmd = new BasicCommand("git", "--version").outputToSysOut(false);
    cmd.run();
    if (cmd.exitValue() == Command.SUCCESS) {
      return true;
    }
    if (GatorGradlePlugin.OS.equals(GatorGradlePlugin.MACOS)) {
      Console.log("You must install Git! An Xcode installation window should open to help you.");
      Console.log(
          "If a window did not open, please visit https://git-scm.com/downloads to get started!");
    } else if (GatorGradlePlugin.OS.equals(GatorGradlePlugin.LINUX)) {
      Console.log(
          "You must install Git! Please issue the following command or visit https://git-scm.com/downloads.");
      Console.log("sudo apt-get install git");
    } else {
      Console.log(
          "You must install Git! Please visit https://git-scm.com/downloads to get started!");
    }
    return false;
  }

  private static boolean doPython() {
    BasicCommand cmd = new BasicCommand("python3", "-V").outputToSysOut(false);
    cmd.run();
    if (cmd.exitValue() == Command.SUCCESS && cmd.getOutput().contains(" 3.")) {
      return true;
    }
    Console.log(
        "You must install Python 3! We recommend using Pyenv, available at https://github.com/pyenv/pyenv.");
    Console.log("You can also visit https://www.python.org/ to download installers for Windows.");
    return false;
  }

  private static boolean doPipenv() {
    BasicCommand pipenv = new BasicCommand("pipenv", "--version").outputToSysOut(false);
    pipenv.run();
    if (pipenv.exitValue() == Command.SUCCESS) {
      return true;
    }
    Console.log(
        "You must install Pipenv! Please visit https://pipenv.readthedocs.io to get started!");
    return false;
  }

  private static boolean doGatorGrader() {
    boolean success = doGatorGraderMain();
    if (!success) {
      GatorGradleCleanTask.deleteGatorGraderInstallation();
    }
    return success;
  }

  private static boolean doGatorGraderMain() {
    Path workingDir = Paths.get(GatorGradlePlugin.GATORGRADER_HOME);

    // quick git fetch installation
    BasicCommand updateOrInstall = new BasicCommand();
    updateOrInstall.outputToSysOut(true).setWorkingDir(workingDir.toFile());
    if (Files.exists(Paths.get(GatorGradlePlugin.GATORGRADER_HOME))) {
      // This could be problematic -- will fetch all current development branches
      // as well, but needed if `version` is pointing to a non-local branch or tag
      updateOrInstall.with("git", "fetch", "--all");
      Console.log("Updating GatorGrader...");
    } else {
      // make dirs
      if (!workingDir.toFile().mkdirs()) {
        Console.error("Failed to make directories: " + workingDir);
      }
      updateOrInstall.with(
          "git", "clone", GATORGRADER_GIT_REPO, GatorGradlePlugin.GATORGRADER_HOME);

      // configure gatorgrader dependencies
      Console.log("Installing GatorGrader...");
    }

    updateOrInstall.run();
    if (updateOrInstall.exitValue() != Command.SUCCESS) {
      error("GatorGrader management failed, could not get updated code!", updateOrInstall);
      return false;
    }

    String revision = GatorGradleConfig.get().getGatorGraderRevision();
    Console.log("Checking out to '" + revision + "'");
    BasicCommand checkout = new BasicCommand("git", "checkout", revision);
    checkout.setWorkingDir(workingDir.toFile());
    checkout.run();
    if (checkout.exitValue() != Command.SUCCESS) {
      error("GatorGrader management failed, could not checkout to '" + revision + "'!", checkout);
      return false;
    }

    checkout = new BasicCommand("git", "pull");
    checkout.setWorkingDir(workingDir.toFile());
    checkout.outputToSysOut(false);
    checkout.run();
    if (checkout.exitValue() != Command.SUCCESS
        && !checkout.getOutput().contains("You are not currently on a branch")) {
      error("GatorGrader management failed, could not update '" + revision + "'!", checkout);
      return false;
    }

    Console.log("Pulling branch...");
    checkout = new BasicCommand("git", "pull");
    checkout.setWorkingDir(workingDir.toFile());
    checkout.outputToSysOut(false);
    checkout.run();
    if (checkout.exitValue() == Command.SUCCESS) {
      Console.log("Updated!");
    } else {
      Console.log("No change.");
    }

    Console.log("Managing GatorGrader's Python dependencies...");
    BasicCommand dep = new BasicCommand("pipenv", "sync", "--bare");
    dep.setWorkingDir(workingDir.toFile());
    dep.outputToSysOut(false);
    dep.run();
    if (dep.exitValue() != Command.SUCCESS) {
      error("GatorGrader management failed, could not install dependencies!", dep);
      return false;
    }
    Console.log("Finished!");
    return true;
  }

  private static void error(String desc, BasicCommand cmd) {
    Console.error("ERROR:", desc);
    Console.error("Command run:", cmd.toString());
    Console.error("OUTPUT:", cmd.getOutput().trim());
  }
}
