package org.gatorgradle.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.gatorgradle.GatorGradlePlugin;
import org.gatorgradle.util.StringUtil;

import org.gradle.api.GradleException;

public class ConfigMap {

  public static final String KEYVAL_SEP = ":";
  public static final String KEYVAL_SEP_REGEX = "(?<!\\)" + KEYVAL_SEP;
  public static final String ESCAPED_KEYVAL_SEP_REGEX = "(?<!\\)\\" + KEYVAL_SEP;
  public static final String MARK_REGEX = "^-{3,}$";

  private static class Line {
    int number;
    String content;
    int indentLevel;

    protected Line(int number, String content) {
      this.number = number;
      this.content = content;
    }

    public void calcIndentLevel(int indentSpacing) {
      if (indentLevel > 0) {
        return;
      }
      int spaces = 0;
      for (int i = 0; i < content.length(); i++) {
        if (content.charAt(i) == ' ') {
          spaces++;
          if (spaces >= indentSpacing) {
            spaces -= indentSpacing;
            indentLevel++;
          }
        } else if (content.charAt(i) == '\t') {
          indentLevel++;
        } else {
          break;
        }
      }
      if (spaces > 0) {
        throw new GradleException(
            "Got " + spaces + " out of place indent spaces on line " + number);
      }
    }

    public boolean isEmpty() {
      return content == null || content.trim().length() == 0;
    }

    public String toString() {
      return content;
    }

    public boolean matches(String regex) {
      return content.matches(regex);
    }
  }

  public static class Value {
    private String value;
    private int lineNumber;

    public Value(String val, int lineNum) {
      value = val;
      lineNumber = lineNum;
    }

    public String asString() {
      return value;
    }

    /**
     * Return the value as a boolean, or throw an error if the value cannot be parsed.
     *
     * @return value as a boolean
     */
    public boolean asBoolean() {
      if (value.matches("[Tt][Rr][Uu][Ee]")) {
        return true;
      } else if (value.matches("[Ff][Aa][Ll][Ss][Ee]")) {
        return false;
      } else {
        throw new GradleException(
            "Failed to parse '" + value + "' on line " + lineNumber + " to boolean value");
      }
    }

    /**
     * Return the value as an integer, or throw an error if the value cannot be parsed.
     *
     * @return value as an integer
     */
    public int asInteger() {
      try {
        return Integer.parseInt(value);
      } catch (NumberFormatException ex) {
        throw new GradleException(
            "Failed to parse '" + value + "' on line " + lineNumber + " to integer value", ex);
      }
    }

    public boolean matches(String regex) {
      return value.matches(regex);
    }

    public int lineNumber() {
      return lineNumber;
    }

    public String toString() {
      return asString();
    }
  }

  // name -> value
  private Map<String, Value> header;
  // path -> checks
  private Map<String, List<Value>> body;

  private int indentSpacing = 4;

  private Path path;

  /**
   * Create a ConfigMap based on the given file.
   *
   * @param path the path to parse for checks/headers/etc
   */
  public ConfigMap(Path path) {
    this.header = new HashMap<>();
    this.body = new HashMap<>();
    this.path = path;
  }

  /**
   * Parses the config file.
   */
  public void parse() {
    try (Stream<String> strLines = Files.lines(path)) {
      final AtomicInteger lineNumber = new AtomicInteger(0);
      List<Line> lines =
          strLines.map(str -> new Line(lineNumber.incrementAndGet(), str))
              .filter(line -> !line.isEmpty() && !line.content.trim().startsWith("#"))
              .collect(Collectors.toList());
      int divider = lines.isEmpty() ? 0 : lines.get(0).number;
      int marks = 0;
      for (int i = 0; i < lines.size(); i++) {
        if (lines.get(i).content.matches(MARK_REGEX)) {
          marks++;
          divider = i;
        }
      }
      divider -= marks - 1;
      lines.removeIf(line -> line.content.matches(MARK_REGEX));

      // parse header
      lines.subList(0, divider).forEach(line -> {
        String[] spl = line.content.split(KEYVAL_SEP_REGEX, 2);
        header.put(
            spl[0].trim().toLowerCase(Locale.ENGLISH),
            new Value(spl[1].trim(), line.number)
        );
      });

      if (hasHeader("indent")) {
        Value indent = getHeader("indent");
        if (indent.matches("[tT][aA][bB]")) {
          indentSpacing = -1;
        } else {
          indentSpacing = indent.asInteger();
        }
      }
      lines.forEach(line -> line.calcIndentLevel(indentSpacing));

      // parse body
      parseBody("", lines.subList(divider, lines.size()));
    } catch (RuntimeException ex) {
      throw new GradleException(
          "Failed to read config file \"" + path + "\": " + ex.getMessage(), ex);
    } catch (Exception ex) {
      throw new GradleException(
          "Failed to read config file \"" + path + "\": " + ex.getMessage(), ex);
    }
  }

  private void parseBody(String path, List<Line> lines) {
    for (int i = 0; i < lines.size(); i++) {
      Line line = lines.get(i);
      if (line.matches(".*\\S+" + KEYVAL_SEP_REGEX + ".*")) {
        // line denotes a path (and maybe value after)
        String[] controls = line.content.trim().split(KEYVAL_SEP_REGEX, 2);
        List<Line> subLines = new ArrayList<>();
        if (controls.length > 1 && controls[1].trim().length() > 0) {
          subLines.add(new Line(
              line.number, StringUtil.repeat('\t', line.indentLevel + 1) + controls[1].trim()));
        }

        while (i + 1 < lines.size() && lines.get(i + 1).indentLevel > line.indentLevel) {
          subLines.add(lines.get(++i));
        }

        parseBody((path.isEmpty() ? "" : path + GatorGradlePlugin.F_SEP) + controls[0], subLines);
      } else {
        // line is a value, add it to the current path
        addCheck(path, new Value(line.content.trim().replaceAll(ESCAPED_KEYVAL_SEP_REGEX, KEYVAL_SEP), line.number));
      }
    }
  }

  private void addCheck(String path, Value value) {
    List<Value> vals = body.get(path);
    if (vals == null) {
      vals = new ArrayList<>();
      vals.add(value);
      body.put(path, vals);
    } else {
      vals.add(value);
    }
  }

  public Value getHeader(String name) {
    return header.get(name);
  }

  public boolean hasHeader(String name) {
    return header.containsKey(name);
  }

  /**
   * Get the values associated with the given path.
   *
   * @param  path the path to get
   * @return      a list of values
   */
  public List<Value> getChecks(String path) {
    return body.get(path);
  }

  /**
   * Get all values in the body.
   *
   * @return a list of all values
   */
  public List<Value> getAllChecks() {
    return body.values().stream().flatMap(list -> list.stream()).collect(Collectors.toList());
  }

  public Set<String> getPaths() {
    return body.keySet();
  }

  /**
   * Give a description of this ConfigMap.
   *
   * @return a descriptive string
   */
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("HEADER:\n");
    header.keySet().forEach(
        key -> builder.append(key).append("=").append(getHeader(key)).append('\n'));
    builder.append("---\nBODY:\n");
    body.keySet().forEach(key -> {
      builder.append(key).append("=[");
      builder.append(String.join(", ",
          getChecks(key).stream().map(val -> val.asString()).toArray(size -> new String[size])));
      builder.append("]\n");
    });
    return builder.toString();
  }
}
