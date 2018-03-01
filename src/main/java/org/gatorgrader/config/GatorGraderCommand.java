package org.gatorgrader.config;

import static org.gatorgrader.GatorGraderPlugin.F_SEP;
import static org.gatorgrader.GatorGraderPlugin.GATORGRADER_HOME;

import org.gatorgrader.Command;

import java.util.Arrays;
import java.util.List;

/**
 * GatorGraderCommand automatically adds the python and gatorgrader path to the beginning of the
 * command.
 */
public class GatorGraderCommand extends Command {
    static String pythonPath;
    static String gatorgraderPath;

    static {
        pythonPath      = "python3";
        gatorgraderPath = GATORGRADER_HOME + F_SEP + "gatorgrader.py";
    }

    public GatorGraderCommand(String... command) {
        this(Arrays.asList(command));
    }

    public GatorGraderCommand(List<String> command) {
        super(pythonPath, gatorgraderPath);
        command.addAll(command);
    }
}
