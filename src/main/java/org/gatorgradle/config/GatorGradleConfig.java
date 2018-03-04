package org.gatorgradle.config;

import org.gatorgradle.GatorGradlePlugin;
import org.gatorgradle.command.BasicCommand;
import org.gatorgradle.command.Command;
import org.gatorgradle.command.GatorGraderCommand;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * GatorGradleConfig holds the configuration for this assignment.
 * TODO: make this configurable via DSL blocks in build.gradle
 */
public class GatorGradleConfig implements Iterable<Command> {
    private static final Pattern commandPattern = Pattern.compile("([^\"]\\S*|\".+?\")\\s*");

    ArrayList<Command> gradingCommands;

    public GatorGradleConfig() {
        gradingCommands = new ArrayList<>();
    }

    /**
     * Create a GatorGradleConfig based on the provided file.
     *
     * @param configFile the file to base this configuration on
     */
    public GatorGradleConfig(File configFile) {
        this();
        // TODO: parse configFile to build gradingCommands
        parseConfigFile(configFile);
    }

    /**
     * Utility method to convert a line of text to a Command.
     *
     * @param  line a line to parse
     * @return      a command
     */
    private static Command lineToCommand(String line) {
        // TODO: this needs major work
        // TODO: parse config file lines, probably in a different way than this

        BasicCommand cmd;
        if (line.toLowerCase().startsWith("gg: ")) {
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

    private void parseConfigFile(File file) {
        try (Stream<String> lines = Files.lines(file.toPath())) {
            lines.filter(line -> line.trim().length() > 0 && !line.startsWith("#"))
                .map(GatorGradleConfig::lineToCommand)
                .forEach((this)::with);
        } catch (IOException ex) {
            // System.err.println("Failed to read in config file!");
            throw new RuntimeException("Failed to read config file \"" + file + "\"");
        }
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
            gradingCommands.stream().map(cmd -> cmd.getDescription()).collect(Collectors.toList()));
    }

    public Iterator<Command> iterator() {
        return gradingCommands.iterator();
    }

    public int size() {
        return gradingCommands.size();
    }
}
