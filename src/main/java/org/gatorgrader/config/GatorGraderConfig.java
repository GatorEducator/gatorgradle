package org.gatorgrader;

import java.io.BufferedReader;
import java.io.File;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * GatorGraderConfig holds the configuration for this assignment.
 * TODO: make this configurable via DSL blocks in build.gradle
 */
public class GatorGraderConfig implements Iterable<Command> {
    ArrayList<Command> gradingCommands;

    public GatorGraderConfig() {
        gradingCommands = new ArrayList<>();
    }

    /**
     * Create a GatorGraderConfig based on the provided file.
     *
     * @param configFile the file to base this configuration on
     */
    public GatorGraderConfig(File configFile) {
        this();
        // TODO: parse configFile to build gradingCommands
        parseConfigFile(configFile);
    }

    private void parseConfigFile(File file) {
        for (int i = 0; i < 100; i++) {
            with(new Command("echo").with("-e").with("" + i + "!").outputToSysOut(true));
            with(new Command("sleep").with("0.5"));
        }
    }

    public GatorGraderConfig with(Command com) {
        gradingCommands.add(com);
        return this;
    }

    public Iterator<Command> iterator() {
        return new ConfigIterator();
    }

    class ConfigIterator implements Iterator<Command> {
        int index;

        public ConfigIterator() {
            index = 0;
        }

        public Command next() {
            return gradingCommands.get(index++);
        }

        public boolean hasNext() {
            return index >= gradingCommands.size();
        }
    }
}
