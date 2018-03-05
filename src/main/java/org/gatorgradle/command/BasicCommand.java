package org.gatorgradle.command;

import org.gradle.api.GradleException;
import org.gradle.api.logging.Logging;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BasicCommand implements Command {
    private static final long serialVersionUID = 6412L;
    private final List<String> command;
    private boolean outSys;
    private File workingDir;

    private String output;
    private transient Thread thread;
    private Callback callback;

    private boolean fin;
    private int exitVal = -1;

    public BasicCommand(final String... command) {
        this.command = new ArrayList<>(Arrays.asList(command));
    }

    public BasicCommand(final List<String> command) {
        this.command = command;
    }

    public BasicCommand with(final String... command) {
        return with(Arrays.asList(command));
    }

    public BasicCommand with(final List<String> command) {
        this.command.addAll(command);
        return this;
    }

    public BasicCommand outputToSysOut(final boolean flag) {
        outSys = flag;
        return this;
    }

    public File getWorkingDir() {
        return workingDir;
    }

    public void setWorkingDir(final File dir) {
        this.workingDir = dir;
    }

    public void setCallback(final Callback callback) {
        this.callback = callback;
    }

    public String getOutput() {
        return output;
    }

    public String getDescription() {
        return "\'" + String.join("\' \'", command) + "\'";
    }

    public int elements() {
        return command.size();
    }

    /**
     * Get the exit value of the command.
     *
     * @return the exit value
     */
    public int exitValue() {
        if (!fin) {
            throw new GradleException("Command not finished, no exit value available!");
        }
        return exitVal;
    }

    /**
     * Get whether the command has finished executing.
     *
     * @return true if the command finished
     */
    public boolean finished() {
        return fin;
    }

    /**
     * Wait for the command to finish.
     *
     * @return the Command that ran/is running
     */
    public BasicCommand waitFor() {
        if (thread != null) {
            try {
                thread.join();
            } catch (InterruptedException ex) {
                System.err.println("Error waiting for command to finish: " + ex);
            }
        }
        return this;
    }

    /**
     * Execute the Command.
     *
     * @param  block should execution block until finished?
     * @return the Command that ran/is running
     */
    public BasicCommand run(boolean block) {
        if (block) {
            run();
        } else {
            thread = new Thread(this);
            thread.start();
        }
        return this;
    }

    /**
     * Run the Command (execute provides better control, and should be called instead of run).
     *
     */
    public void run() {
        fin = false;
        if (command.isEmpty()) {
            throw new RuntimeException("Empty command run!");
        }

        ProcessBuilder pb = new ProcessBuilder(command);
        if (workingDir != null) {
            pb.directory(workingDir);
        }
        pb.redirectErrorStream(true);
        BufferedReader in = null;
        try {
            Process proc = pb.start();

            in = new BufferedReader(new InputStreamReader(proc.getInputStream(), "UTF-8"));

            StringBuilder out = new StringBuilder();
            int newChar;
            while (true) {
                newChar = in.read();
                if (!(newChar > 0)) {
                    break;
                }
                out.append((char) newChar);
                if (outSys) {
                    System.out.print((char) newChar);
                }
            }

            proc.waitFor();
            exitVal = proc.exitValue();
            output  = out.toString().trim();

        } catch (InterruptedException | IOException ex) {
            // don't do anything fancy with logs
            // if (ex.getMessage().contains("error=2")) {
            //     output.append("Error: Command not found: \'")
            //         .append(command.stream().collect(Collectors.joining(" ")))
            //         .append("\'\n");
            //     if (outSys) {
            //         System.out.print("Error: Command not found: \'");
            //         System.out.print(command.stream().collect(Collectors.joining(" ")));
            //         System.out.print("\'\n");
            //     }
            //     // command not found exit code
            //     exitVal = 127;
            // } else {
            //     System.err.print("Error while running '");
            //     System.err.print(command.stream().collect(Collectors.joining("\' \'")));
            //     System.err.println("':");
            //     ex.printStackTrace();
            //     exitVal = -1;
            // }
            // System.err.println("Exception while running " + getDescription() + ": " + ex);
            Logging.getLogger(BasicCommand.class)
                .error("Exception while running {}: {}", getDescription(), ex);
        } finally {
            fin = true;
            if (callback != null) {
                callback.accept(this);
            }
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                System.err.println("Failed to close command input stream!");
            }
        }
    }

    /**
     * Run user demonstration of Command usage.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        BasicCommand com = new BasicCommand(args);
        com.outputToSysOut(false);
        com.run(true);
        System.out.print("OUTPUT:\n" + com.getOutput());
        System.out.println("EXIT VALUE: " + com.exitValue());
    }
}
