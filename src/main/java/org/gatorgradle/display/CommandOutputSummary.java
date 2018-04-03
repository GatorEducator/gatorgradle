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
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    boolean nomore = false;

    /**
     * Output a description of what just finished, or maybe a status.
     *
     * @param cmd the command that just finished
     */
    public void showInProgressSummary(Command cmd) {
        if (nomore) {
            return;
        }
        boolean fail = printCommandResult(cmd);
        if (fail && GatorGradleConfig.get().shouldBreakBuild()) {
            log.lifecycle("\n  -~-  \u001B[1;31mCHECKS FAILED\u001B[0m  -~-\n");
            nomore = true;
            throw new RuntimeException("Check failed, ending execution!");
        }
    }

    // TODO: parse this better
    private boolean printCommandResult(Command cmd) {
        // debug output for TAs
        log.info("COMMAND: {}", cmd.getDescription());
        log.info("EXIT VALUE: {}", cmd.exitValue());
        log.info("OUTPUT:");
        // actual output of the command should be parsed and colored, etc
        if (cmd instanceof BasicCommand) {
            String output = parseCommandOutput((BasicCommand) cmd);
            log.lifecycle(output);
            // log.warn(StringUtil.clamp(output, 80));
            // if (output.length() > 80) {
            //     log.info("…" + output.substring(79));
            // }
        }

        if (cmd.exitValue() != Command.SUCCESS) {
            log.info("Check failed ({})!", cmd.exitValue());
            return true;
        }
        return false;
    }

    /**
     * Output the compiled summary to the project's Logger.
     */
    public void showOutputSummary() {
        log.lifecycle("\n  -~-  \u001B[1;36mBeginning check summary\u001B[1;0m  -~-\n");

        List<Command> failed = completedCommands.stream()
                                   .filter(cmd -> cmd.exitValue() != Command.SUCCESS)
                                   .collect(Collectors.toList());
        boolean mis = completedCommands.removeAll(failed);

        for (int i = 0; i < completedCommands.size(); i++) {
            // failure needs to be handled
            printCommandResult(completedCommands.get(i));
            if (i < completedCommands.size() - 1) {
                log.lifecycle(" ~-~-~ ");
            }
        }

        failed.forEach(cmd -> {
            log.lifecycle(" ~-~-~ ");
            printCommandResult(cmd);
        });

        String text =
            mis ? "Failed some checks for " + GatorGradleConfig.get().getAssignmentName() + "!"
                : "Passed all checks for " + GatorGradleConfig.get().getAssignmentName() + "!";
        int textLen = text.length();
        text        = (mis ? "\u001B[1;31m" : "\u001B[1;32m") + text + "\u001B[0m";

        char upleft    = '\u250f'; // upper left corner
        char upright   = '\u2513'; // upper right corner
        char downleft  = '\u2517'; // lower left corner
        char downright = '\u251B'; // lower right corner
        char vert      = '\u2503'; // vertical line
        char horz      = '\u2501'; // horizontal line

        String above = "\u001B[1;35m" + upleft + StringUtil.repeat(horz, textLen + 2) + upright
                       + "\u001B[0m"; // line above
        String before = "\u001B[1;35m" + vert + "\u001B[0m "; // vertical line
        String after  = " \u001B[1;35m" + vert + "\u001B[0m"; // vertical line
        String below  = "\u001B[1;35m" + downleft + StringUtil.repeat(horz, textLen + 2) + downright
                       + "\u001B[0m"; // line below

        log.warn("\n\n\t{}\n\t{}{}{}\n\t{}", above, before, text, after, below);
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
