package org.gatorgradle.command;

import java.io.Serializable;

public interface Command extends Runnable, Serializable {
    public static final int SUCCESS = 0;

    public abstract int exitValue();

    public abstract boolean finished();

    public abstract Command waitFor();

    public abstract String description();
}
