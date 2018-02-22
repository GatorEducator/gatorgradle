package org.gatorgrader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Command implements Runnable {
    private List<String> command;
    private boolean outputToSysOut = false;

    private StringBuilder output = new StringBuilder();
    private Thread thread;

    private boolean fin = false;
    private int exitVal = 0;

    public Command(String... command) {
        this.command = new ArrayList<>(Arrays.asList(command));
    }

    public Command(List<String> args) {
        this.command = args;
    }

    public Command() {
        this.command = new ArrayList<>();
    }

    public Command with(String... command) {
        this.command.addAll(Arrays.asList(command));
        return this;
    }

    public String getOutput() {
        return output.toString();
    }

    public void outputToSysOut(boolean val) {
        outputToSysOut = val;
    }

    /**
     * Get the exit value of the command.
     *
     * @return the exit value
     */
    public int exitValue() {
        if (!fin) {
            throw new RuntimeException("Command not finished, no exit value available!");
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
     */
    public void waitFor() {
        if (thread != null) {
            try {
                thread.join();
            } catch (InterruptedException ex) {
                System.err.println("Error waiting for command to finish: " + ex);
            }
        }
    }

    /**
     * Execute the Command.
     *
     * @param block should this method block until the command finishes?
     */
    public void execute(boolean block) {
        if (block) {
            run();
        } else {
            thread = new Thread(this);
            thread.start();
        }
    }

    /**
     * Run the Command (execute provides better control, and should be called instead of run).
     *
     */
    public void run() {
        if (command.isEmpty()) {
            throw new RuntimeException("Empty command run!");
        }

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        try {
            Process proc = pb.start();

            BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));

            int newChar;
            while (true) {
                newChar = in.read();
                if (!(newChar > 0)) {
                    break;
                }
                output.append((char) newChar);
                if (outputToSysOut) {
                    System.out.print((char) newChar);
                }
            }

            proc.waitFor();
            exitVal = proc.exitValue();

        } catch (InterruptedException | IOException ex) {
            if (ex.getMessage().contains("error=2")) {
                output.append("Error: Command not found: \'")
                    .append(command.stream().collect(Collectors.joining(" ")))
                    .append("\'\n");
                if (outputToSysOut) {
                    System.out.println("Error: Command not found!");
                }
                // command not found exit code
                exitVal = 127;
            } else {
                System.err.print("Error while running '");
                System.err.print(command.stream().collect(Collectors.joining("\' \'")));
                System.err.println("':");
                ex.printStackTrace();
                exitVal = -1;
            }
        } finally {
            fin = true;
        }
    }

    /**
     * Run user demonstration of Command usage.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        Command com = new Command(args);
        com.outputToSysOut(false);
        com.execute(true);
        System.out.print("OUTPUT:\n" + com.getOutput());
        System.out.println("EXIT VALUE: " + com.exitValue());
    }
}
