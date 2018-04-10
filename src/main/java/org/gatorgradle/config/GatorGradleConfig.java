package org.gatorgradle.config;

import org.gatorgradle.GatorGradlePlugin;
import org.gatorgradle.command.*;
import org.gatorgradle.util.Console;

import org.gradle.api.GradleException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * GatorGradleConfig holds the configuration for this assignment.
 * TODO: make this configurable via DSL blocks in build.gradle
 */
public class GatorGradleConfig implements Iterable<Command> {
    private static GatorGradleConfig singleton;

    /**
     * Get the config.
     *
     * @return the config
     */
    public static GatorGradleConfig get() {
        if (singleton != null) {
            return singleton;
        }
        throw new RuntimeException("GatorGradleConfig not created");
    }

    /**
     * Create the config by parsing the given file.
     *
     * @param  configFile the file to be parsed
     * @return            the config
     */
    public static GatorGradleConfig create(Path configFile) {
        singleton = new GatorGradleConfig(configFile);
        return singleton;
    }

    private static final Pattern commandPattern = Pattern.compile("([^\"]\\S*|\".+?\")\\s*");

    private boolean breakBuild    = false;
    private String assignmentName = "this assignment";

    List<Command> gradingCommands;
    ConfigMap file;

    private GatorGradleConfig() {
        gradingCommands = new ArrayList<>();
    }

    /**
     * Create a GatorGradleConfig based on the provided file.
     *
     * @param configFile the file to base this configuration on
     */
    private GatorGradleConfig(Path configFile) {
        this();
        // TODO: parse configFile to build gradingCommands
        this.file = new ConfigMap(configFile);
    }

    /**
     * Create a config that will use the given values.
     *
     * @param breakBuild     should the build break on check failures
     * @param assignmentName the assignment name
     * @param commands       the list of commands to run
     */
    public GatorGradleConfig(boolean breakBuild, String assignmentName, List<Command> commands) {
        this.breakBuild      = breakBuild;
        this.assignmentName  = assignmentName;
        this.gradingCommands = new ArrayList<>(commands);
    }

    /**
     * Utility method to convert a line of text to a Command.
     *
     * @param  line a line to parse
     * @return      a command
     */
    private static Command makeCommand(String path, String line) {
        // need to deal with adding checkfiles and directories associated with path
        BasicCommand cmd;
        if (line.toLowerCase(Locale.ENGLISH).startsWith("gg: ")) {
            line = line.substring(4);
            cmd  = new GatorGraderCommand().outputToSysOut(false);
        } else {
            cmd = new BasicCommand().outputToSysOut(false);
        }
        Matcher mtc = commandPattern.matcher(line);
        while (mtc.find()) {
            cmd.with(mtc.group(1).replace("\"", ""));
        }
        return cmd;
    }

    /**
     * Parses the config file.
     */
    public void parse() {
        file.parse();
        assignmentName = file.getHeader("name").asString();
        breakBuild     = file.getHeader("break").asBoolean();

        file.getPaths().forEach(
            path -> file.getChecks(path).forEach(val -> with(makeCommand(path, val.asString()))));
    }

    /**
     * Add a command to this config.
     *
     * @param  cmd the command to add
     * @return     the current config after adding
     */
    public GatorGradleConfig with(Command cmd) {
        gradingCommands.add(cmd);
        return this;
    }

    public String toString() {
        return String.join(" -> ",
            gradingCommands.stream().map(cmd -> cmd.toString()).collect(Collectors.toList()));
    }

    public Iterator<Command> iterator() {
        return gradingCommands.iterator();
    }

    public boolean shouldBreakBuild() {
        return breakBuild;
    }

    public String getAssignmentName() {
        return assignmentName;
    }

    public int size() {
        return gradingCommands.size();
    }
}
