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
      BasicCommand query = new BasicCommand(getPipenv(), "--venv");
      query.setWorkingDir(new File(GatorGradlePlugin.GATORGRADER_HOME));
      query.outputToSysOut(false);
      query.run(true);
      if (query.exitValue() != 0) {
        error("Query for python executable failed -- try reinstalling GatorGrader", query);
        throw new GradleException("Failed to run pipenv --venv! -- Was GatorGrader installed?");
      }
      PYTHON_EXECUTABLE = query.getOutput() + "/bin/python";
    }
    return PYTHON_EXECUTABLE;
  }

  /**
   * Returns the Pipenv executable path.
   *
   * @return the path
   */
  public static String getPipenv() {
    // BasicCommand userBaseQuery = new BasicCommand("python3", "-m", "site", "--user-base");
    // userBaseQuery.outputToSysOut(false);
    // userBaseQuery.run();
    //
    // if (userBaseQuery.exitValue() != Command.SUCCESS) {
    //   error("Failed to retrieve user-base location!", userBaseQuery);
    //   return null;
    // }
    //
    // return userBaseQuery.getOutput() + "/bin/pipenv";
    //
    // FIXME: QUICK WORKAROUND

    return GatorGradlePlugin.USER_HOME + "/.local/bin/pipenv";
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
      // gatorgrader repo exists (most likely)
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
      error("GatorGrader management failed! Perhaps git wasn't installed?", updateOrInstall);
      return false;
    }

    //TODO: refactor to include pip and Pipenv as Dependencies in their own right
    if (doDeps) {
      Console.log("Installing GatorGrader dependencies...");
      Console.log("Ensuring pip is installed...");
      BasicCommand pip = new BasicCommand("pip", "-V").outputToSysOut(false);
      pip.run();
      if (pip.exitValue() != Command.SUCCESS) {
        // pip is disabled or otherwise failed, try and see if ensurepip is available
        pip = new BasicCommand("python3", "-m", "ensurepip");
        pip.outputToSysOut(true);
        pip.run();

        if (pip.exitValue() != Command.SUCCESS) {
          error("GatorGrader management failed, could not install pip!", pip);
          return false;
        }
      }

      Console.log("Ensuring Pipenv is installed...");
      BasicCommand pipenv = new BasicCommand(getPipenv(), "--version").outputToSysOut(false);
      pipenv.run();
      if (pipenv.exitValue() != Command.SUCCESS) {
        // pipenv is disabled or otherwise failed, try and install it
        if (GatorGradlePlugin.OS != GatorGradlePlugin.LINUX) {
          Console.log("You must install Pipenv! Please visit https://pipenv.readthedocs.io/en/latest/ to get started!");
          return false;
        } else {
          pipenv = new BasicCommand("pip", "install", "pipenv");
          pipenv.outputToSysOut(true);
          pipenv.run();

          if (pipenv.exitValue() != Command.SUCCESS) {
            error("GatorGrader management failed, could not install Pipenv!", pipenv);
            return false;
          }
        }
      }

      // String userBase = addPipenvBin();
      // if (userBase == null) {
      //   return false;
      // }

      Console.log("Installing dependencies...");
      BasicCommand dep = new BasicCommand(getPipenv(), "install");
      dep.setWorkingDir(new File(GatorGradlePlugin.GATORGRADER_HOME));
      dep.outputToSysOut(true);
      dep.run();
      Console.log("Finished GatorGrader install!");
      if (dep.exitValue() != Command.SUCCESS) {
        error("GatorGrader management failed, could not install dependencies!", dep);
        return false;
      }
    }

    Console.newline(2);
    return true;
  }

  // private static String addPipenvBin() {
  //   Console.log("Adding python user-base bin to $PATH in ~/.bashrc");
  //   Console.log("(ATTENTION: source the python user-base bin folder for other shells)");
  //
  //   BasicCommand userBaseQuery = new BasicCommand("python3", "-m", "site", "--user-base");
  //   userBaseQuery.outputToSysOut(false);
  //   userBaseQuery.run();
  //
  //   if (userBaseQuery.exitValue() != Command.SUCCESS) {
  //     error("Failed to retrieve user-base location!", userBaseQuery);
  //     return null;
  //   }
  //
  //   String userBase = userBaseQuery.getOutput() + "/bin";
  //   String bashPathUpdate = "export PATH=\"$PATH:" + userBase + "\";";
  //
  //   Path bashrc = Paths.get(GatorGradlePlugin.USER_HOME + "/.bashrc");
  //
  //   Console.log("Updating " + bashrc + "...");
  //   try {
  //     if (Files.lines(bashrc).anyMatch(line -> line.contains(bashPathUpdate))) {
  //       Console.log("~/.bashrc already contains required path addition!");
  //       return userBase;
  //     }
  //   } catch (IOException ex) {
  //     Console.log("Failed to read ~/.bashrc");
  //     return null;
  //   }
  //
  //   try {
  //     Files.write(bashrc, ("\n" + bashPathUpdate).getBytes("UTF-8"), StandardOpenOption.APPEND);
  //     return userBase;
  //   } catch (IOException e) {
  //     Console.log("Failed to write to ~/.bashrc");
  //     return null;
  //   }
  // }

  private static void error(String desc, BasicCommand cmd) {
    Console.error("ERROR:", desc);
    Console.error("Command run:", cmd.toString());
    Console.error("OUTPUT:", cmd.getOutput().trim());
  }
}
