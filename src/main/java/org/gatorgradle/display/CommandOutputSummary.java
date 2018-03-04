package org.gatorgradle.display;

import org.gatorgradle.command.BasicCommand;
import org.gatorgradle.command.Command;
import org.gatorgradle.util.StringUtil;

import org.gradle.api.Project;
import org.gradle.api.logging.Logger;

import java.util.List;

public class CommandOutputSummary {
    List<Command> completedCommands;
    Project project;

    public CommandOutputSummary(List<Command> completedCommands, Project project) {
        this.completedCommands = completedCommands;
        this.project           = project;
    }

    /**
     * Output the compiled summary to the project's Logger.
     */
    public void showOutputSummary() {
        Logger log = project.getLogger();
        for (Command cmd : completedCommands) {
            // failure needs to be handled
            if (cmd.exitValue() != 0) {
                log.error("Command failed with code " + cmd.exitValue()
                          + "!\nCommand description: " + cmd.getDescription());
            }
            // debug output for TAs
            log.debug("COMMAND " + cmd.getDescription());
            log.debug("EXIT VALUE: " + cmd.exitValue());

            // actual output of the command should be parsed and colored, etc
            if (cmd instanceof BasicCommand) {
                log.lifecycle(StringUtil.clamp(((BasicCommand) cmd).getOutput(), 80));
            }
        }
    }
}
