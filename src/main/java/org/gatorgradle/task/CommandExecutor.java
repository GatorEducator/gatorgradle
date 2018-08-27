package org.gatorgradle.task;

import org.gatorgradle.command.Command;
import org.gradle.workers.WorkerExecutor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

public class CommandExecutor implements Runnable {
  private Command command;

  @Inject
  public CommandExecutor(Command command) {
    this.command = command;
  }

  /**
   * Implements the run method from Runnable. This method executes the gathered Commands.
   */
  @Override
  public void run() {
    command.run();
  }
}
