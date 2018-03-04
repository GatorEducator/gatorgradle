package org.gatorgradle.display;

import org.gatorgradle.command.BasicCommand;
import org.gatorgradle.command.Command;
import org.gatorgradle.task.GatorGradleTask;
import org.gatorgradle.util.StringUtil;

import org.gradle.api.Project;
import org.gradle.api.logging.Logger;

import java.util.List;

public class CommandOutputSummary {
    private List<Command> completedCommands;
    private final Logger log;

    public CommandOutputSummary(List<Command> completedCommands, Logger log) {
        this.completedCommands = completedCommands;
        this.log               = log;
    }

    /**
     * Output the compiled summary to the project's Logger.
     */
    public void showOutputSummary() {
        for (Command cmd : completedCommands) {
            // failure needs to be handled
            if (cmd.exitValue() != 0) {
                log.warn("Check failed with code {}!\nCommand description: {}", cmd.exitValue(),
                    cmd.getDescription());
            }
            // debug output for TAs
            log.debug("COMMAND {}", cmd.getDescription());
            log.debug("EXIT VALUE: {}", cmd.exitValue());

            // actual output of the command should be parsed and colored, etc
            if (cmd instanceof BasicCommand) {
                log.quiet(StringUtil.clamp(((BasicCommand) cmd).getOutput(), 80));
            }
        }
    }
}
