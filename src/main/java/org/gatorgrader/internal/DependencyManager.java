package org.gatorgrader.internal;

import static org.gatorgrader.GatorGraderPlugin.F_SEP;
import static org.gatorgrader.GatorGraderPlugin.GATORGRADER_HOME;
import static org.gatorgrader.GatorGraderPlugin.USER_HOME;

import org.gatorgrader.Command;

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

    private static boolean doGatorGrader() {
        // Temporary gatorgrader home for manual installation
        GATORGRADER_HOME = USER_HOME + F_SEP + ".gatorgrader";

        // quick git pull installation
        Command updateOrInstall = new Command().workingDir(USER_HOME);
        if (Files.exists(Paths.get(GATORGRADER_HOME))) {
            // gatorgrader repo exists (most likely)
            updateOrInstall.with("git", "pull");
        } else {
            updateOrInstall.with("git", "clone", "https://github.com/gkapfham/gatorgrader.git", GATORGRADER_HOME);
        }

        // install gatorgrader, and block until complete (FIXME: this needs to be better)
        updateOrInstall.run(true);
        if (updateOrInstall.exitValue() != Command.SUCCESS) {
            System.err.println("ERROR! updateOrInstall failed! Output:");
            System.err.println(updateOrInstall.getOutput());
            return false;
        } else {
            return true;
        }
    }
}
