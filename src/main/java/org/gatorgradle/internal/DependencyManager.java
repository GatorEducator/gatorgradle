package org.gatorgradle.internal;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;

import org.gatorgradle.GatorGradlePlugin;
import org.gatorgradle.command.BasicCommand;
import org.gatorgradle.command.Command;
import org.gatorgradle.util.Console;

import org.gradle.api.GradleException;


public class DependencyManager {
  public static final String GATORGRADER_GIT_REPO =
      "https://github.com/GatorEducator/gatorgrader.git";
  private static String PYTHON_EXECUTABLE = null;

  /**
   * Returns the python executable path.
   *
   * @return the path
   */
  public static String getPython() {
    if (PYTHON_EXECUTABLE == null) {
      BasicCommand query = new BasicCommand("pipenv", "--venv");
      query.setWorkingDir(new File(GatorGradlePlugin.GATORGRADER_HOME));
      query.outputToSysOut(false);
      query.run(true);
      if (query.exitValue() != 0) {
        error("Query for python executable failed -- try reinstalling GatorGrader", query);
        throw new GradleException("Failed to run pipenv --venv! -- Was GatorGrader installed?");
      }
      if (GatorGradlePlugin.OS.equals(GatorGradlePlugin.WINDOWS)) {
        PYTHON_EXECUTABLE = query.getOutput() + GatorGradlePlugin.F_SEP + "Scripts"
            + GatorGradlePlugin.F_SEP + "python";
      } else {
        PYTHON_EXECUTABLE = query.getOutput() + GatorGradlePlugin.F_SEP + "bin"
            + GatorGradlePlugin.F_SEP + "python";
      }
    }
    return PYTHON_EXECUTABLE;
  }

  /**
   * Install or Update the given dependency.
   *
   * @param  dep the dependency to update or install
   * @return     a boolean indicating success or failure
   */
  public static boolean installOrUpdate(Dependency dep) {
    switch (dep) {
      case GATORGRADER:
        return doGatorGrader();
      case PYTHON:
        return doPython();
      case PIPENV:
        return doPipenv();
      case GIT:
        return doGit();
      default:
        Console.error("Unsupported Dependency: " + dep);
        return false;
    }
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
          "You must install Git! Please issue the below command or visit https://git-scm.com/downloads.");
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
        "You must install Python 3! We recommend using Pyenv (https://github.com/pyenv/pyenv).");
    Console.log("You can also visit https://www.python.org/ to download Windows installers.");
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
      Path path = Paths.get(GatorGradlePlugin.GATORGRADER_HOME);
      Console.log("Deleting " + path);
      try {
        Files.walk(path)
            .sorted(Comparator.reverseOrder())
            .map(innerPath -> innerPath.toFile())
            .forEach(file -> {
              boolean deleted = file.delete();
              if (!deleted) {
                Console.error("Did not delete " + path + "!");
              }
            });
      } catch (IOException ex) {
        Console.error("Failed to delete " + path + "!");
      }
    }
    return success;
  }

  private static boolean doGatorGraderMain() {
    Path workingDir = Paths.get(GatorGradlePlugin.GATORGRADER_HOME);

    boolean doDeps = false;

    // quick git pull installation
    BasicCommand updateOrInstall = new BasicCommand();
    updateOrInstall.outputToSysOut(true).setWorkingDir(workingDir.toFile());
    if (Files.exists(Paths.get(GatorGradlePlugin.GATORGRADER_HOME))) {
      // FIXME: should we checkout to the master branch?
      // BasicCommand checkout = new BasicCommand("git", "checkout", "master");
      // checkout.run();
      // if (checkout.exitValue() != Command.SUCCESS) {
      //   error("GatorGrader management failed, could not checkout 'master' branch!", checkout);
      // }
      updateOrInstall.with("git", "pull");
      Console.log("Updating GatorGrader...");
    } else {
      // make dirs
      if (!workingDir.toFile().mkdirs()) {
        Console.error("Failed to make directories: " + workingDir);
      }
      updateOrInstall.with(
          "git", "clone", GATORGRADER_GIT_REPO, GatorGradlePlugin.GATORGRADER_HOME);

      // configure gatorgrader dependencies
      doDeps = true;
      Console.log("Installing GatorGrader...");
    }

    updateOrInstall.run();
    if (updateOrInstall.exitValue() != Command.SUCCESS) {
      error("GatorGrader management failed!", updateOrInstall);
      return false;
    }

    if (doDeps) {
      Console.log("Installing GatorGrader python dependencies...");
      BasicCommand dep = new BasicCommand("pipenv", "sync");
      dep.setWorkingDir(new File(GatorGradlePlugin.GATORGRADER_HOME));
      dep.outputToSysOut(true);
      dep.run();
      Console.log("Finished GatorGrader install!");
      if (dep.exitValue() != Command.SUCCESS) {
        error("GatorGrader management failed, could not install dependencies!", dep);
        return false;
      }
    } else {
      Console.log("Updating GatorGrader python dependencies...");
      BasicCommand dep = new BasicCommand("pipenv", "sync");
      dep.setWorkingDir(new File(GatorGradlePlugin.GATORGRADER_HOME));
      dep.outputToSysOut(true);
      dep.run();
      Console.log("Finished GatorGrader update!");
    }

    Console.newline(2);
    return true;
  }

  private static void error(String desc, BasicCommand cmd) {
    Console.error("ERROR:", desc);
    Console.error("Command run:", cmd.toString());
    Console.error("OUTPUT:", cmd.getOutput().trim());
  }
}
