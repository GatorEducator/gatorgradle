package org.gatorgradle.command;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.gatorgradle.util.Console;
import org.gradle.api.GradleException;
import org.gradle.api.logging.Logging;

public class BasicCommand implements Command {
  private static final long serialVersionUID = 6412L;
  private final List<String> command;
  private boolean outSys;
  private File workingDir;

  private String output;
  private transient Thread thread = null;
  private Callback callback;

  private boolean fin;
  private int exitVal = -1;

  public BasicCommand(final String... command) {
    this.command = new ArrayList<>(Arrays.asList(command));
  }

  public BasicCommand(final Collection<String> command) {
    this.command = new ArrayList<>(command);
  }

  public BasicCommand with(final String... command) {
    return with(Arrays.asList(command));
  }

  public BasicCommand with(final Collection<String> command) {
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

  /**
   * Builds a descriptive string by recreating the command run.
   *
   * @return a descriptive string
   */
  public String toString() {
    List<String> strs = new ArrayList<>(command);
    strs.replaceAll(str -> str.matches("\\S+") ? str : "'" + str + "'");
    return "[" + String.join(" ", strs) + "]";
  }

  public String executable() {
    return command.get(0);
  }

  public String last() {
    return command.get(command.size() - 1);
  }

  public int elements() {
    return command.size();
  }

  /**
   * Tests the object for equality.
   *
   * @param cmd the object to test
   * @return true if the object represents the same textual command
   */
  public boolean equals(Object cmd) {
    if (cmd instanceof BasicCommand) {
      return command.equals(((BasicCommand) cmd).command);
    } else {
      return false;
    }
  }

  /**
   * Calculated the hashcode.
   *
   * @return the hashcode
   */
  public int hashCode() {
    return command.stream()
        .map(str -> str.hashCode())
        .reduce(1, (first, second) -> 17 * first + 29 * second);
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
        Console.error("Error waiting for command to finish: " + ex);
      }
    }
    return this;
  }

  /**
   * Execute the Command.
   *
   * @param block should execution block until finished?
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

  /** Execute the Command, blocking. */
  public void run() {
    fin = false;
    if (command.isEmpty()) {
      throw new RuntimeException("Empty command run!");
    }

    // final long startTime = System.nanoTime();

    ProcessBuilder pb = new ProcessBuilder(command);
    if (workingDir != null) {
      pb.directory(workingDir);
    }
    pb.redirectErrorStream(true);
    BufferedReader in = null;
    StringBuilder out = null;
    try {
      Process proc = pb.start();

      in = new BufferedReader(new InputStreamReader(proc.getInputStream(), "UTF-8"));

      out = new StringBuilder();
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

    } catch (Throwable thr) {
      Logging.getLogger(BasicCommand.class)
          .error("Exception while running {}: {}", toString(), thr.toString());
      exitVal = 127;
    } finally {
      fin = true;

      if (out != null) {
        output = out.toString();
      }
      if (callback != null) {
        callback.accept(this);
      }
      try {
        if (in != null) {
          in.close();
        }
      } catch (IOException ex) {
        Console.error("Failed to close command input stream!");
      }
    }

    // Console.log("Command " + toString() + " finished in "
    // + String.format("%.2fms!", (System.nanoTime() - startTime) / 1_000_000d));
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
    Console.log("EXIT VALUE: " + com.exitValue());
  }
}
