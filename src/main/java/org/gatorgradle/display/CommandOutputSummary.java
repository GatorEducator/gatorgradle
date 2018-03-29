package org.gatorgradle.display;

import org.gatorgradle.command.BasicCommand;
import org.gatorgradle.command.Command;
import org.gatorgradle.task.GatorGradleTask;
import org.gatorgradle.util.*;

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
        boolean mis = false;
        String yes  = "\u001B[1;32mYes\u001B[0m";
        String no   = "\u001B[1;31mNo\u001B[0m";

        Console.log("\n  --  \u001B[1;36mBeginning check summary\u001B[1;0m  --  \n");

        for (Command cmd : completedCommands) {
            // failure needs to be handled
            if (cmd.exitValue() != 0) {
                log.info("Check failed with code {}!\nCommand description: {}", cmd.exitValue(),
                    cmd.getDescription());
                mis = true;
            }
            // debug output for TAs
            log.debug("COMMAND {}", cmd.getDescription());
            log.debug("EXIT VALUE: {}", cmd.exitValue());

            // actual output of the command should be parsed and colored, etc
            if (cmd instanceof BasicCommand) {
                String output = ((BasicCommand) cmd).getOutput();
                log.lifecycle(output.replace("Yes", yes).replace("No", no).trim());
                // log.warn(StringUtil.clamp(output, 80));
                // if (output.length() > 80) {
                //     log.info("…" + output.substring(79));
                // }
            }

            log.lifecycle("\n ~-~-~ \n");
        }

        log.warn("\u001B[1;33mOverall, are there any mistakes in the assignment? {}\u001B[0m",
            mis ? "\u001B[1;31mYes" : "\u001B[32mNo");
    }
}
