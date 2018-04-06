package org.gatorgradle.internal;

import static org.gatorgradle.GatorGradlePlugin.*;

import org.gatorgradle.command.BasicCommand;
import org.gatorgradle.command.Command;
import org.gatorgradle.util.Console;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DependencyManager {
    public static final String GATORGRADER_GIT_REPO = "https://github.com/gkapfham/gatorgrader.git";

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
        if (OS.equals(MACOS)) {
            Console.log(
                "You must install Git! An Xcode installation window should open to help you.");
            Console.log(
                "If a window did not open, please visit https://git-scm.com/downloads to get started!");
        } else if (OS.equals(LINUX)) {
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
        Path workingDir = Paths.get(GATORGRADER_HOME);

        boolean doDeps = false;

        // quick git pull installation
        BasicCommand updateOrInstall = new BasicCommand();
        updateOrInstall.outputToSysOut(true).setWorkingDir(workingDir.toFile());
        if (Files.exists(Paths.get(GATORGRADER_HOME))) {
            // gatorgrader repo exists (most likely)
            BasicCommand checkout = new BasicCommand("git", "checkout", "master");
            checkout.run();
            if (checkout.exitValue() != Command.SUCCESS) {
                error(
                    "GatorGrader management failed, could not checkout 'master' branch!", checkout);
            }
            updateOrInstall.with("git", "pull");
            Console.log("Updating GatorGrader...");
        } else {
            // make dirs
            if (!workingDir.toFile().mkdirs()) {
                Console.error("Failed to make directories: " + workingDir);
            }
            updateOrInstall.with("git", "clone", GATORGRADER_GIT_REPO, GATORGRADER_HOME);

            // configure gatorgrader dependencies
            doDeps = true;
            Console.log("Installing GatorGrader...");
        }

        // install gatorgrader, and block until complete (FIXME: this needs to be better)
        updateOrInstall.run(true);
        if (updateOrInstall.exitValue() != Command.SUCCESS) {
            error("GatorGrader management failed! Perhaps we couldn't find git?", updateOrInstall);
            return false;
        }

        if (doDeps) {
            // TODO: look into using pipenv or other virtual environment - can we activate those
            // environments from java and have it continue to be activated for subsequent commands?

            Console.log("Installing GatorGrader dependencies...");
            BasicCommand pip = new BasicCommand("python3", "-m", "ensurepip");
            pip.outputToSysOut(true);
            pip.run();
            if (pip.exitValue() != Command.SUCCESS) {
                error("GatorGrader management failed, could not install dependencies!", pip);
                return false;
            }
            BasicCommand dep = new BasicCommand(
                "python3", "-m", "pip", "install", "-r", GATORGRADER_HOME + "/requirements.txt");
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

    private static void error(String desc, BasicCommand cmd) {
        Console.error("ERROR:", desc);
        Console.error("Command run:", cmd.toString());
        Console.error("OUTPUT:", cmd.getOutput().trim());
    }
}
