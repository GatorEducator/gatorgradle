package org.gatorgradle.command;

import static org.gatorgradle.GatorGradlePlugin.F_SEP;
import static org.gatorgradle.GatorGradlePlugin.GATORGRADER_HOME;

import org.gatorgradle.internal.DependencyManager;

import java.util.Arrays;
import java.util.List;

/**
 * GatorGraderCommand automatically adds the python and gatorgrader path to the beginning of the
 * command.
 */
public class GatorGraderCommand extends BasicCommand {
    private static final long serialVersionUID = 2142L;

    private static final String WELCOME_FLAG = "--nowelcome";
    private static final String JSON_FLAG    = "--json";

    public GatorGraderCommand(String... command) {
        this(Arrays.asList(command));
    }

    /**
     * Create a command from a list of strings.
     *
     * @param command the list of arguments
     **/
    public GatorGraderCommand(List<String> command) {
        super(DependencyManager.getPython(), GATORGRADER_HOME + F_SEP + "gatorgrader.py",
            WELCOME_FLAG, JSON_FLAG);
        super.with(command);
    }
}
