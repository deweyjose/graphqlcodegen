package io.github.deweyjose.graphqlcodegen;

import org.apache.maven.plugin.logging.Log;
import org.slf4j.helpers.MessageFormatter;

public class Slf4jMavenLogger {
  private static volatile Log mavenLog;

  private Slf4jMavenLogger() {}

  public static void registerMavenLog(Log log) {
    mavenLog = log;
  }

  public static void info(String format, Object... args) {
    if (mavenLog != null) {
      mavenLog.info(MessageFormatter.arrayFormat(format, args).getMessage());
    }
  }

  public static void debug(String format, Object... args) {
    if (mavenLog != null) {
      mavenLog.debug(MessageFormatter.arrayFormat(format, args).getMessage());
    }
  }

  public static void warn(String format, Object... args) {
    if (mavenLog != null) {
      mavenLog.warn(MessageFormatter.arrayFormat(format, args).getMessage());
    }
  }

  public static void error(String format, Object... args) {
    if (mavenLog != null) {
      mavenLog.error(MessageFormatter.arrayFormat(format, args).getMessage());
    }
  }
}
