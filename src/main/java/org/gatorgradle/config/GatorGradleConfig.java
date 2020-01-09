package org.gatorgradle.config;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.gatorgradle.command.BasicCommand;
import org.gatorgradle.command.Command;
import org.gatorgradle.command.GatorGraderCommand;

import org.gradle.api.GradleException;

/**
 * GatorGradleConfig holds the configuration for this assignment.
 * TODO: make this configurable via DSL blocks in build.gradle
 */
public class GatorGradleConfig implements Iterable<Command> {

  private static GatorGradleConfig singleton;

  /**
   * Get the config.
   *
   * @return the config
   */
  public static GatorGradleConfig get() {
    if (singleton != null) {
      return singleton;
    }
    throw new RuntimeException("GatorGradleConfig not created");
  }

  /**
   * Create the config by parsing the given file.
   *
   * @param  configFile the file to be parsed
   * @return            the config
   */
  public static GatorGradleConfig create(Path configFile) {
    singleton = new GatorGradleConfig(configFile);
    return singleton;
  }

  private static final Pattern commandPattern = Pattern.compile("([^\"]\\S*|\".+?\")\\s*");
  private static final String pureIndicator = "(pure)";

  private boolean breakBuild = false;
  private boolean fastBreakBuild = false;
  private String assignmentName = "this assignment";
  private String gatorgraderRevision = "master";
  private String reportEndpoint = System.getenv("GATOR_ENDPOINT");
  private String reportApiKey = System.getenv("GATOR_API_KEY");
  private String idCommand = "git config --global user.email";
  private String reflectionPath = null;
  private Collection<String> commandLineExecutables;
  private Command startupCommand = null;
  private Set<Command> gradingCommands;
  private ConfigMap file;

  private GatorGradleConfig() {
    gradingCommands = new HashSet<>();
    commandLineExecutables = new HashSet<>();
    commandLineExecutables.add("mdl");
    commandLineExecutables.add("htmlhint");
    commandLineExecutables.add("proselint");
  }

  /**
   * Create a GatorGradleConfig based on the provided file.
   *
   * @param configFile the file to base this configuration on
   */
  private GatorGradleConfig(Path configFile) {
    this();
    this.file = new ConfigMap(configFile);
  }

  /**
   * Utility method to convert a line of text to a Command.
   *
   * @param  path the path in the config file this line is in the context of
   * @param  line a line to parse
   * @return      a command
   */
  private Command makeCommand(String path, String line) {
    return makeCommand(path, line, false);
  }

  /**
   * Utility method to convert a line of text to a Command.
   *
   * @param  path the path in the config file this line is in the context of
   * @param  line a line to parse
   * @param  pure this command is a pure command (pass directly to shell)
   * @return      a command
   */
  private Command makeCommand(String path, String line, boolean pure) {
    // need to deal with adding checkfiles and directories associated with path
    if (path == null) {
      path = "";
    }
    List<String> splits = new ArrayList<>();
    Matcher mtc = commandPattern.matcher(line);
    while (mtc.find()) {
      splits.add(mtc.group(1).replace("\"", ""));
    }

    // FIXME: there should be a better method of determining which path separator is used
    // in the config file -- it is independent of the OS.
    int sep = Math.max(path.lastIndexOf("/"), path.lastIndexOf("\\"));
    String name = path;
    String dir = "";
    if (sep >= 0) {
      name = path.substring(sep + 1);
      dir = path.substring(0, sep);
    }
    BasicCommand cmd;
    if (pureIndicator.equals(splits.get(0)) || pure) {
      if (pureIndicator.equals(splits.get(0))) {
        splits.remove(0);
      }
      cmd = new BasicCommand();
      cmd.outputToSysOut(false);
      if (path.length() > 0) {
        File workDir = new File(path);
        if (!workDir.isDirectory()) {
          throw new GradleException(
              "Pure command '" + line + "' inside path '"
              + path + "' must be in a directory context"
          );
        }
        cmd.setWorkingDir(workDir);
      }
    } else if (commandLineExecutables.contains(splits.get(0))) {
      cmd = new BasicCommand();
      cmd.outputToSysOut(false);
      splits.add(path.length() > 0 ? path : ".");
    } else {
      cmd = new GatorGraderCommand();
      cmd.outputToSysOut(false);
      if (name.length() > 0) {
        splits.add("--file");
        splits.add(name);
      }
      if (path.length() > 0) {
        splits.add("--directory");
        splits.add(dir);
      }
    }

    cmd.with(splits);

    return cmd;
  }


  /**
   * Parses the config file's header.
   */
  public void parseHeader() {
    file.parse();
    assignmentName = file.getHeader("name").asString();

    if (file.hasHeader("break")) {
      breakBuild = file.getHeader("break").asBoolean();
    }

    if (file.hasHeader("fastfail")) {
      fastBreakBuild = file.getHeader("fastfail").asBoolean();
    }

    if (file.hasHeader("idcommand")) {
      idCommand = file.getHeader("idcommand").asString();
    } else if (file.hasHeader("idcmd")) {
      idCommand = file.getHeader("idcmd").asString();
    }

    if (file.hasHeader("revision")) {
      gatorgraderRevision = file.getHeader("revision").asString();
    } else if (file.hasHeader("version")) {
      gatorgraderRevision = file.getHeader("version").asString();
    }

    if (file.hasHeader("reflection")) {
      reflectionPath = file.getHeader("reflection").asString();
    }

    if (file.hasHeader("executables")) {
      List<String> lst = Arrays.asList(file.getHeader("executables").asString().split(","));
      lst.replaceAll(String::trim);
      commandLineExecutables.addAll(lst);
    }

    if (file.hasHeader("startup")) {
      startupCommand = makeCommand(null, file.getHeader("startup").asString(), true);
    }
  }

  /**
   * Parses the config file's body.
   */
  public void parseBody() {

    file.getPaths().forEach(
        path -> file.getChecks(path).forEach(val -> with(makeCommand(path, val.asString()))));
  }

  /**
   * Parse the entire configuration file.
   */
  public void parse() {
    parseHeader();
    parseBody();
  }

  /**
   * Add a command to this config.
   *
   * @param  cmd the command to add
   * @return     the current config after adding
   */
  public GatorGradleConfig with(Command cmd) {
    gradingCommands.add(cmd);
    return this;
  }

  /**
   * Gives a string representation of this config.
   *
   * @return a descriptive string
   */
  public String toString() {
    return file.toString() + "\n\nCOMMANDS:"
        + String.join("\n-> ",
              gradingCommands.stream().map(cmd -> cmd.toString()).collect(Collectors.toList()));
  }

  public Iterator<Command> iterator() {
    return gradingCommands.iterator();
  }

  public boolean shouldBreakBuild() {
    return breakBuild;
  }

  public boolean shouldFastBreakBuild() {
    return fastBreakBuild;
  }

  public String getIdCommand() {
    return idCommand;
  }

  public String getAssignmentName() {
    return assignmentName;
  }

  public String getGatorGraderRevision() {
    return gatorgraderRevision;
  }

  public boolean hasStartupCommand() {
    return startupCommand != null;
  }

  public Command getStartupCommand() {
    return startupCommand;
  }

  public String getReflectionPath() {
    return reflectionPath;
  }

  public String getReportEndpoint() {
    return reportEndpoint;
  }

  public String getReportApiKey() {
    return reportApiKey;
  }

  public boolean isCommandLineExecutable(String exec) {
    return commandLineExecutables.contains(exec);
  }

  public int size() {
    return gradingCommands.size();
  }
}
