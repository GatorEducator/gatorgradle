package org.gatorgradle.display;

import org.gatorgradle.GatorGradlePlugin;
import org.gatorgradle.command.*;
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
            log.info("Duplicate command: " + cmd.toString());
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
        boolean fail = printCommandResult(cmd, true);
        if (fail && GatorGradleConfig.get().shouldBreakBuild()) {
            log.lifecycle("\n  -~-  \u001B[1;31mCHECKS FAILED\u001B[0m  -~-\n");
            nomore = true;
            throw new RuntimeException("Check failed, ending execution!");
        }
    }

    // TODO: parse this better
    private boolean printCommandResult(Command cmd, boolean sep) {
        // debug output for TAs
        log.info("COMMAND: {}", cmd.toString());
        log.info("EXIT VALUE: {}", cmd.exitValue());
        log.info("OUTPUT:");
        // actual output of the command should be parsed and colored, etc

        if (cmd instanceof BasicCommand) {
            String output = parseCommandOutput((BasicCommand) cmd);
            log.lifecycle(output);
            if (sep) {
                log.lifecycle("\u001B[1;36m ~-~-~ \u001B[0m");
            }
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
        // log.lifecycle("\n\n  -~-  \u001B[1;36mBeginning check summary\u001B[1;0m  -~-\n\n");
        int totalChecks      = completedCommands.size();
        List<Command> failed = completedCommands.stream()
                                   .filter(cmd -> cmd.exitValue() != Command.SUCCESS)
                                   .collect(Collectors.toList());
        boolean mis = failed.size() > 0;

        if (mis) {
            log.lifecycle(
                "\n\n\u001B[1;33m  -~-  \u001B[1;31mFAILURES  \u001B[1;33m-~-\u001B[0m\n");
            for (int i = 0; i < failed.size(); i++) {
                printCommandResult(failed.get(i), false);
                if (i < failed.size() - 1) {
                    log.lifecycle("\u001B[1;36m ~-~-~ \u001B[0m");
                }
            }
        }

        StringUtil.border("Passed " + (completedCommands.size() - failed.size()) + "/" + totalChecks
                              + " of checks for " + GatorGradleConfig.get().getAssignmentName()
                              + "!",
            mis ? "\u001B[1;31m" : "\u001B[1;32m", mis ? "\u001B[1;35m" : "\u001B[1;32m", log);
    }

    private String parseCommandOutput(BasicCommand cmd) {
        String output = cmd.getOutput();

        boolean gatorgrader = cmd instanceof GatorGraderCommand;

        List<String> pots = new ArrayList<>();
        if (gatorgrader && output != null) {
            Scanner scan = new Scanner(output);
            while (scan.hasNext()) {
                String potential = scan.nextLine();
                if (potential.toLowerCase(Locale.ENGLISH).contains("yes")
                    || potential.toLowerCase(Locale.ENGLISH).contains("no")) {
                    pots.add(potential);
                }
            }
        } else if (GatorGradleConfig.PROGRAMS.contains(cmd.executable())) {
            StringBuilder builder = new StringBuilder();
            builder.append("Does ")
                .append(cmd.last())
                .append(" pass ")
                .append(cmd.executable())
                .append("? ")
                .append(cmd.exitValue() == cmd.SUCCESS ? YES : NO);
            if (output != null && !output.isEmpty()) {
                Scanner scan = new Scanner(cmd.getOutput());
                builder.append("\n\u001B[1;31m  ---\u001B[0m\n\u001B[33m");
                while (scan.hasNext()) {
                    builder.append("  ").append(scan.nextLine()).append("\n");
                }
                builder.append("\u001B[0m\u001B[1;31m  ---\u001B[0m");
            }
            output = builder.toString();
        } else {
            output = "Does " + cmd + " pass? " + (cmd.exitValue() == cmd.SUCCESS ? YES : NO);
        }

        // always return last line with a yes or no, FIXME: fix this when GatorGrader has atomic
        // checks or json reporting (when CUT_OUTPUT is true)
        output = CUT_OUTPUT && pots.size() > 0 && gatorgrader ? pots.get(pots.size() - 1) : output;

        return gatorgrader
            ? output.trim().replaceAll("\\b[Yy][Ee][Ss]\\b", YES).replaceAll("\\b[Nn][Oo]\\b", NO)
            : output;
    }
}
