package org.gatorgradle.config;

import org.gatorgradle.util.StringUtil;

import org.gradle.api.GradleException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConfigMap {
    public static final String KEYVAL_SEP = ":";
    public static final String MARK_REGEX = "^-{3,}$";

    private static class Line {
        int number;
        String content;
        int indentLevel;

        protected Line(int number, String content, int indentSpacing) {
            this.number  = number;
            this.content = content;
            int spaces   = 0;
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
                    "Got " + spaces + " extra indent spaces on line " + number);
            }
        }

        public boolean isEmpty() {
            return content == null || content.length() == 0;
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
            value      = val;
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
                    "Failed to parse '" + value + "' on line " + lineNumber + " to integer value",
                    ex);
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
    private Map<String, List<Value>> filechecks;

    private int indentSpacing = 4;

    private Path path;

    /**
     * Create a ConfigMap based on the given file.
     *
     * @param path the path to parse for checks/headers/etc
     */
    public ConfigMap(Path path) {
        this.header     = new HashMap<>();
        this.filechecks = new HashMap<>();
        this.path       = path;
    }

    /**
     * Parses the config file.
     */
    public void parse() {
        try (Stream<String> strLines = Files.lines(path)) {
            final AtomicInteger lineNumber = new AtomicInteger(0);
            List<Line> lines =
                strLines.map(str -> new Line(lineNumber.incrementAndGet(), str, indentSpacing))
                    .filter(line -> !line.isEmpty() && !line.content.startsWith("#"))
                    .collect(Collectors.toList());
            int divider = lines.isEmpty() ? 0 : lines.get(0).number;
            int marks   = 0;
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
                String[] spl = line.content.split(KEYVAL_SEP, 2);
                header.put(spl[0].trim(), new Value(spl[1].trim(), line.number));
            });

            if (hasHeader("indent")) {
                Value indent = getHeader("indent");
                if (indent.matches("[tT][aA][bB]")) {
                    indentSpacing = -1;
                } else {
                    indentSpacing = indent.asInteger();
                }
            }

            // parse body
            parseBody("", lines.subList(divider, lines.size()));
        } catch (RuntimeException ex) {
            throw new GradleException("Failed to read config file \"" + path + "\"", ex);
        } catch (Exception ex) {
            throw new GradleException("Failed to read config file \"" + path + "\"", ex);
        }
    }

    private void parseBody(String path, List<Line> lines) {
        for (int i = 0; i < lines.size(); i++) {
            Line line = lines.get(i);
            if (line.matches(".*\\S+" + KEYVAL_SEP + ".*")) {
                // line denotes a path (and maybe value after)
                String[] controls   = line.content.trim().split(KEYVAL_SEP, 2);
                String subPath      = controls[0];
                List<Line> subLines = new ArrayList<>();
                if (controls.length > 1) {
                    subLines.add(new Line(line.number,
                        StringUtil.repeat('\t', line.indentLevel + 1) + controls[1].trim(),
                        indentSpacing));
                }
                parseBody(path + "/" + subPath, subLines);

            } else {
                // line is a value, add it to the current path
                if (!filechecks.containsKey(path)) {
                    filechecks.put(path, new ArrayList<>());
                }
                filechecks.get(path).add(new Value(line.content.trim(), line.number));
            }
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
        return filechecks.get(path);
    }

    /**
     * Get all values in the body.
     *
     * @return a list of all values
     */
    public List<Value> getAllChecks() {
        return filechecks.values()
            .stream()
            .flatMap(list -> list.stream())
            .collect(Collectors.toList());
    }

    public Set<String> getPaths() {
        return filechecks.keySet();
    }
}
