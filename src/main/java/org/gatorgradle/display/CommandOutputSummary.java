package org.gatorgradle.display;

import org.gatorgradle.GatorGradlePlugin;
import org.gatorgradle.command.BasicCommand;
import org.gatorgradle.command.Command;
import org.gatorgradle.config.GatorGradleConfig;
import org.gatorgradle.task.GatorGradleTask;
import org.gatorgradle.util.*;

import org.gradle.api.Project;
import org.gradle.api.logging.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public class CommandOutputSummary {
    private static final String YES   = "\u001B[1;32mYes\u001B[0m";
    private static final String NO    = "\u001B[1;31mNo\u001B[0m";
    private static boolean CUT_OUTPUT = true;

    private List<Command> completedCommands;
    private final Logger log;

    public CommandOutputSummary(Logger log) {
        this.completedCommands = new ArrayList<>();
        this.log               = log;
    }

    public CommandOutputSummary(List<Command> completedCommands, Logger log) {
        this.completedCommands = new ArrayList<>(completedCommands);
        this.log               = log;
    }

    /**
     * Add the command to the summary.
     *
     * @param cmd the command to add
     */
    public void addCompletedCommand(Command cmd) {
        if (!completedCommands.contains(cmd)) {
            completedCommands.add(cmd);
            showInProgressSummary(cmd);
        } else {
            log.info("Duplicate command: " + cmd.getDescription());
        }
    }

    public int getNumCompletedTasks() {
        return completedCommands.size();
    }

    /**
     * Output a description of what just finished, or maybe a status.
     *
     * @param cmd the command that just finished
     */
    public void showInProgressSummary(Command cmd) {
        if (cmd.exitValue() != 0) {
            log.info("Check failed ({})!\nCommand description: {}", cmd.exitValue(),
                cmd.getDescription());
            if (GatorGradleConfig.shouldBreakBuild()) {
                throw new RuntimeException("Check failed, ending execution!");
            }
        }

        // TODO: parse this better
        if (cmd instanceof BasicCommand) {
            String output = parseCommandOutput((BasicCommand) cmd);
            log.lifecycle(output);
            // log.warn(StringUtil.clamp(output, 80));
            // if (output.length() > 80) {
            //     log.info("…" + output.substring(79));
            // }
        }
    }

    /**
     * Output the compiled summary to the project's Logger.
     */
    public void showOutputSummary() {
        boolean mis = false;

        Console.log("\n  -~-  \u001B[1;36mBeginning check summary\u001B[1;0m  -~-  \n");

        // completedCommands.sort((first, second) -> second.hashCode() - first.hashCode());

        for (Command cmd : completedCommands) {
            // failure needs to be handled
            if (cmd.exitValue() != 0) {
                log.info("Check failed ({})!\nCommand description: {}", cmd.exitValue(),
                    cmd.getDescription());
                mis = true;
            }
            // debug output for TAs
            log.info("COMMAND {}", cmd.getDescription());
            log.info("EXIT VALUE: {}", cmd.exitValue());

            // actual output of the command should be parsed and colored, etc
            if (cmd instanceof BasicCommand) {
                String output = parseCommandOutput((BasicCommand) cmd);
                log.lifecycle(output);
                // log.warn(StringUtil.clamp(output, 80));
                // if (output.length() > 80) {
                //     log.info("…" + output.substring(79));
                // }
            }

            log.lifecycle(" ~-~-~ ");
        }

        log.warn("\u001B[1;33mOverall, does this assignment pass every grading check? {}",
            mis ? NO : YES);
    }

    private String parseCommandOutput(BasicCommand cmd) {
        Scanner scan      = new Scanner(cmd.getOutput());
        List<String> pots = new ArrayList<>();
        pots.add("No output for " + cmd.getDescription());
        while (scan.hasNext()) {
            String potential = scan.nextLine();
            if (potential.toLowerCase(Locale.ENGLISH).contains("yes")
                || potential.toLowerCase(Locale.ENGLISH).contains("no")) {
                pots.add(potential);
            }
        }

        if (CUT_OUTPUT) {
            // always return last line with a yes or no, TODO fix this when GatorGrader has atomic
            // checks
            return pots.get(pots.size() - 1)
                .trim()
                .replaceAll("[Yy][Ee][Ss]", YES)
                .replaceAll("[Nn][Oo]", NO);
        }

        return cmd.getOutput().trim().replaceAll("[Yy][Ee][Ss]", YES).replaceAll("[Nn][Oo]", NO);
    }
}
