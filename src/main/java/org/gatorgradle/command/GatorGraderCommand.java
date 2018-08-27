package org.gatorgradle.command;

import java.util.Arrays;
import java.util.List;

import org.gatorgradle.GatorGradlePlugin;

import org.gatorgradle.internal.DependencyManager;

/**
 * GatorGraderCommand automatically adds the python and gatorgrader path to the beginning of the
 * command.
 */
public class GatorGraderCommand extends BasicCommand {
  private static final long serialVersionUID = 2142L;

  private static final String WELCOME_FLAG = "--nowelcome";
  private static final String JSON_FLAG = "--json";

  public GatorGraderCommand(String... command) {
    this(Arrays.asList(command));
  }

  /**
   * Create a command from a list of strings.
   *
   * @param command the list of arguments
   **/
  public GatorGraderCommand(List<String> command) {
    super(DependencyManager.getPython(),
        GatorGradlePlugin.GATORGRADER_HOME + GatorGradlePlugin.F_SEP + "gatorgrader.py",
        WELCOME_FLAG, JSON_FLAG);
    super.with(command);
  }
}
