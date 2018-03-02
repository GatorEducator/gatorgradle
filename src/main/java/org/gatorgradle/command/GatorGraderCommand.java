package org.gatorgradle.command;

import static org.gatorgradle.GatorGradlePlugin.F_SEP;
import static org.gatorgradle.GatorGradlePlugin.GATORGRADER_HOME;

import java.util.Arrays;
import java.util.List;

/**
 * GatorGraderCommand automatically adds the python and gatorgrader path to the beginning of the
 * command.
 * TODO: make python3 a Dependency, create method to get default executable path in
 * DependencyManager
 */
public class GatorGraderCommand extends BasicCommand {
    public GatorGraderCommand(String... command) {
        this(Arrays.asList(command));
    }

    public GatorGraderCommand(List<String> command) {
        super("python3", GATORGRADER_HOME + F_SEP + "gatorgrader.py", "--nowelcome");
        super.with(command);
    }
}
