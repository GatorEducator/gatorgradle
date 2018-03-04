package org.gatorgradle.command;

import java.io.File;
import java.io.Serializable;
import java.util.function.Consumer;

public interface Command extends Runnable, Serializable {
    public static interface Callback extends Consumer<Command>, Serializable {}

    public static final int SUCCESS = 0;

    public abstract int exitValue();

    public abstract boolean finished();

    public abstract Command waitFor();

    public abstract String getDescription();

    public abstract File getWorkingDir();

    public abstract void setWorkingDir(File dir);

    public abstract void setCallback(Callback callback);
}
