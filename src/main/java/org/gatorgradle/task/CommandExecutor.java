package org.gatorgradle.task;

import org.gatorgradle.command.Command;
import org.gradle.api.provider.Property;
import org.gradle.workers.WorkAction;
import org.gradle.workers.WorkParameters;

public abstract class CommandExecutor
    implements WorkAction<CommandExecutor.CommandExecutorParameters> {

  @Override
  public void execute() {
    getParameters().getCommand().get().run();
  }

  public static interface CommandExecutorParameters extends WorkParameters {
    Property<Command> getCommand();
  }
}
