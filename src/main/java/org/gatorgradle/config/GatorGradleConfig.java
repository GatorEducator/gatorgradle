package org.gatorgradle.config;

import org.gatorgradle.command.BasicCommand;
import org.gatorgradle.command.Command;
import org.gatorgradle.command.GatorGraderCommand;

import java.io.BufferedReader;
import java.io.File;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.stream.Collectors;

/**
 * GatorGradleConfig holds the configuration for this assignment.
 * TODO: make this configurable via DSL blocks in build.gradle
 */
public class GatorGradleConfig implements Iterable<Command> {
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

    private void parseConfigFile(File file) {
        for (int i = 0; i < 5000; i++) {
            with(new BasicCommand("echo").with("-e").with("" + i + "!").outputToSysOut(true));
            // with(new BasicCommand("sleep").with("0.5"));
        }
    }

    public GatorGradleConfig with(Command com) {
        gradingCommands.add(com);
        return this;
    }

    public String toString() {
        return String.join(" -> ",
            gradingCommands.stream().map(com -> com.description()).collect(Collectors.toList()));
    }

    public Iterator<Command> iterator() {
        return gradingCommands.iterator();
    }
}
