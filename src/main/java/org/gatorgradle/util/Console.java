package org.gatorgradle.util;

import java.io.PrintStream;

import java.util.stream.Stream;

public class Console {
  /**
   * Log a message.
   *
   * @param logs Any objects to log
   */
  public static void log(Object... logs) {
    logToStream(System.out, logs);
  }

  /**
   * Log an error.
   *
   * @param logs Any objects to log
   */
  public static void error(Object... logs) {
    logToStream(System.err, logs);
  }

  /**
   * Move the log cursor the specified number of new lines down.
   *
   * @param num The number of new lines to log
   */
  public static void newline(int num) {
    num--;
    String str = "";
    if (num > 0) {
      str = StringUtil.repeat('\n', num);
    }
    log(str);
  }

  public static void newline() {
    newline(1);
  }

  /**
   * Log a message to a specified stream.
   *
   * @param out  The stream to log to
   * @param logs Any objects to log
   */
  public static void logToStream(PrintStream out, Object... logs) {
    if (logs.length > 0) {
      out.println(String.join(
          " ", Stream.of(logs).map(obj -> obj.toString()).toArray(size -> new String[size])));
    }
  }
}
