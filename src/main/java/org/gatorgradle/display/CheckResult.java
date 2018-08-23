package org.gatorgradle.display;

import org.gatorgradle.util.*;

import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

public class CheckResult {
    public static class MalformedJsonException extends Exception {
        public MalformedJsonException(String json) {
            super("Failed to parse json:\n\"" + json + "\"");
        }

        public MalformedJsonException(String reason, String json) {
            super(
                "Failed to parse json -- " + reason + (json != null ? ":\n\"" + json + "\"" : ""));
        }
    }

    public String check;
    public Boolean outcome;
    public String diagnostic;

    /**
     * Construct a CheckResult.
     *
     * @param check      the check completed
     * @param outcome    the outcome of the check
     * @param diagnostic the diagnostic output from the check
     **/
    public CheckResult(String check, Boolean outcome, String diagnostic) {
        this.check      = check;
        this.outcome    = outcome;
        this.diagnostic = diagnostic;
    }

    /**
     * Parses the given JSON string, setting check, outcome,
     * and diagnostic member variables when found accordingly.
     *
     * <p>This method assumes the json passed represents only one
     * result, and any duplicates will overwrite values.
     *
     * @param json the json to parse
     * @throws MalformedJsonException if the json given is not valid for a CheckResult
     *
     */
    @SuppressWarnings("unchecked")
    public CheckResult(String json) throws MalformedJsonException {
        try {
            // Extremely hacky implementation using nashorn scripting to
            // not require external dependencies or do JSON parsing here.
            ScriptEngine engine     = new ScriptEngineManager().getEngineByName("nashorn");
            json                    = json.replaceAll("\"", "\\\"");
            json                    = json.replaceAll("'", "\\'");
            String script           = "Java.asJSONCompatible(" + json + ")";
            Object evaled           = engine.eval(script);
            Map<String, Object> map = (Map<String, Object>) evaled;
            this.check              = (String) map.get("check");
            this.outcome            = (Boolean) map.get("outcome");
            this.diagnostic         = (String) map.get("diagnostic");
        } catch (Throwable ex) {
            throw new MalformedJsonException(ex.getMessage(), json);
        }

        if (check == null || outcome == null || diagnostic == null) {
            throw new MalformedJsonException("required values not present in", json);
        }
    }

    public static final String PASS_SYMBOL =
        StringUtil.color(StringUtil.GOOD, "\u2714"); // heavy check (✔)
    public static final String FAIL_SYMBOL =
        StringUtil.color(StringUtil.BAD, "\u2718"); // heavy cross (✘)
    public static final String FIX_SYMBOL =
        StringUtil.color("\u001B[1;33m", "\u2794"); // right arrow (➔)

    /**
     * Returns a string representation of this result.
     *
     * @param includeDiagnostic include diagnostic?
     * @return a string
     **/
    public String textReport(boolean includeDiagnostic) {
        if (outcome) {
            return PASS_SYMBOL + "  " + check;
        } else {
            String output = FAIL_SYMBOL + "  " + check;
            if (includeDiagnostic) {
                output +=
                    "\n   " + FIX_SYMBOL + "  " + StringUtil.color(StringUtil.FIX, diagnostic);
            }
            return output;
        }
    }
}
