package org.gatorgradle.task;

import javax.inject.Inject;

import org.gatorgradle.command.Command;

import org.gradle.workers.WorkerExecutor;

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
