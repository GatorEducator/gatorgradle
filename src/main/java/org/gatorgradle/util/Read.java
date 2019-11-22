package org.gatorgradle.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Read {


  /**
   * Read in reflection file
   *
   * @param fileName the path to the reflection file
   * @return the reflection file in string with json escaped
   */
  public static String readFile(String fileName){
    try {
      String reflection = String.join("\n", Files.readAllLines(Paths.get(fileName)));
      return StringUtil.jsonEscape(reflection);
    } catch (IOException IOException) {
      IOException.printStackTrace();
    }
  }
}
