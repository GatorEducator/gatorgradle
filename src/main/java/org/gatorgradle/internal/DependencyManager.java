package org.gatorgradle.internal;

import static org.gatorgradle.GatorGradlePlugin.F_SEP;
import static org.gatorgradle.GatorGradlePlugin.GATORGRADER_HOME;
import static org.gatorgradle.GatorGradlePlugin.OS;
import static org.gatorgradle.GatorGradlePlugin.USER_HOME;

import org.gatorgradle.command.BasicCommand;
import org.gatorgradle.command.Command;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

public class DependencyManager {
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
            default:
                System.err.println("Unsupported Dependency: " + dep);
                return false;
        }
    }

    // TODO: should we install git if it isn't available?

    private static String getGitExecutable() {
        if (OS.equals("linux")) {
            BasicCommand cmd = new BasicCommand().outputToSysOut(false);
            cmd.with("which", "git");
            cmd.run();
            if (cmd.exitValue() == 0) {
                return cmd.getOutput();
            } else {
                System.err.println("Could not find git!");
                return "git";
            }
        } else {
            // what are some sensible defaults?
            // can we test homebrew on mac?
            // what about windows?
            // for now lets try for just in the path
            System.err.println("Finding git is not supported on " + OS + "!");
            return "git";
        }
    }

    private static boolean doGatorGrader() {
        if (!OS.equals("linux")) {
            System.err.println("Automated installation unsupported for non-Linux OSes");
            return false;
        }

        String git = getGitExecutable();

        // quick git pull installation
        BasicCommand updateOrInstall = new BasicCommand();
        updateOrInstall.outputToSysOut(false).setWorkingDir(new File(GATORGRADER_HOME));
        if (Files.exists(Paths.get(GATORGRADER_HOME))) {
            // gatorgrader repo exists (most likely)
            updateOrInstall.with(git, "pull");
        } else {
            // FIXME: will need to update url
            updateOrInstall.with(
                git, "clone", "https://github.com/gkapfham/gatorgrader.git", GATORGRADER_HOME);
        }

        // install gatorgrader, and block until complete (FIXME: this needs to be better)
        updateOrInstall.run(true);
        if (updateOrInstall.exitValue() != Command.SUCCESS) {
            System.err.println(
                "ERROR! GatorGrader management failed! Perhaps we couldn't find git?");
            System.err.println("Command run: " + updateOrInstall.getDescription());
            System.err.println("OUTPUT: " + updateOrInstall.getOutput());
            return false;
        } else {
            return true;
        }
    }
}
