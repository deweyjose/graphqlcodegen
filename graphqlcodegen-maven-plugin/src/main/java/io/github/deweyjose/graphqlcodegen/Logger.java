package io.github.deweyjose.graphqlcodegen;

import org.apache.maven.plugin.logging.Log;
import org.slf4j.helpers.MessageFormatter;

/**
 * Logger class for logging messages to the Maven logger.
 */
public class Logger {
  private static volatile Log mavenLog;

  /**
   * Private constructor to prevent instantiation.
   */
  private Logger() {}

  /**
   * Registers the Maven logger.
   *
   * @param log the Maven logger
   */
  public static void registerMavenLog(Log log) {
    mavenLog = log;
  }

  /**
   * Logs an info message.
   *
   * @param format the format string
   * @param args the arguments
   */
  public static void info(String format, Object... args) {
    if (mavenLog != null) {
      mavenLog.info(MessageFormatter.arrayFormat(format, args).getMessage());
    }
  }

  /**
   * Logs a debug message.
   *
   * @param format the format string
   * @param args the arguments
   */
  public static void debug(String format, Object... args) {
    if (mavenLog != null) {
      mavenLog.debug(MessageFormatter.arrayFormat(format, args).getMessage());
    }
  }

  /**
   * Logs a warning message.
   *
   * @param format the format string
   * @param args the arguments
   */
  public static void warn(String format, Object... args) {
    if (mavenLog != null) {
      mavenLog.warn(MessageFormatter.arrayFormat(format, args).getMessage());
    }
  }

  /**
   * Logs an error message.
   * @param format
   * @param args
   */
  public static void error(String format, Object... args) {
    if (mavenLog != null) {
      mavenLog.error(MessageFormatter.arrayFormat(format, args).getMessage());
    }
  }
}
