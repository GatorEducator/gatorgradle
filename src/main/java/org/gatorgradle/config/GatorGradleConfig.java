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
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
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
    public static final Collection<String> PROGRAMS;
    public static final Collection<String> FILENAME_EXCLUSIONS;

    static {
        Collection<String> set = new HashSet<>();
        set.add("mdl");
        set.add("htmlhint");
        set.add("proselint");
        PROGRAMS = Collections.unmodifiableCollection(set);

        set = new HashSet<>();
        set.add("gg");
        FILENAME_EXCLUSIONS = Collections.unmodifiableCollection(set);
    }

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

    Set<Command> gradingCommands;
    ConfigMap file;

    private GatorGradleConfig() {
        gradingCommands = new HashSet<>();
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
    public GatorGradleConfig(
        boolean breakBuild, String assignmentName, Collection<Command> commands) {
        this.breakBuild      = breakBuild;
        this.assignmentName  = assignmentName;
        this.gradingCommands = new HashSet<>(commands);
    }

    /**
     * Utility method to convert a line of text to a Command.
     *
     * @param  line a line to parse
     * @return      a command
     */
    private static Command makeCommand(String path, String line) {
        // need to deal with adding checkfiles and directories associated with path

        List<String> splits = new ArrayList<>();
        Matcher mtc         = commandPattern.matcher(line);
        while (mtc.find()) {
            splits.add(mtc.group(1).replace("\"", ""));
        }

        int sep     = path.lastIndexOf(GatorGradlePlugin.F_SEP);
        String name = path;
        String dir  = "";
        if (sep >= 0) {
            name = path.substring(sep + 1);
            dir  = path.substring(0, sep);
        }
        BasicCommand cmd;
        if (PROGRAMS.contains(splits.get(0))) {
            cmd = new BasicCommand().outputToSysOut(false);
            splits.add(path.length() > 0 ? path : ".");
        } else {
            cmd = new GatorGraderCommand().outputToSysOut(false);
            if (name.length() > 0 && !FILENAME_EXCLUSIONS.contains(name)) {
                splits.add(0, name);
                splits.add(0, "--file");
            }
            if (path.length() > 0) {
                splits.add(0, dir);
                splits.add(0, "--directory");
            }
        }

        cmd.with(splits);

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

    /**
     * Gives a string representation of this config.
     *
     * @return a descriptive string
     */
    public String toString() {
        return file.toString() + "\n\nCOMMANDS:"
            + String.join("\n-> ",
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
