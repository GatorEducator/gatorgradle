package org.gatorgradle.command;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public interface Command extends Runnable, Serializable {
    public static interface Callback extends Consumer<Command>, Serializable {}

    public static final int SUCCESS = 0;

    public abstract int exitValue();

    public abstract boolean finished();

    public abstract Command waitFor();

    public abstract File getWorkingDir();

    public abstract void setWorkingDir(File dir);

    public abstract void setCallback(Callback callback);

    public static List<Command> list(String... cmds) {
        String[][] parsed = new String[cmds.length][];
        return list(parsed);
    }

    /**
     * Build a list of commands from the given string arrays.
     *
     * @param  cmds The commands to make the list out of
     * @return      the list of commands
     */
    public static List<Command> list(String[]... cmds) {
        List<Command> commands = new ArrayList<>();

        for (String[] cmd : cmds) {
            commands.add(create(cmd));
        }

        return commands;
    }

    public static Command create(String... cmd) {
        return new BasicCommand(cmd);
    }
}
