package io.github.deweyjose.graphqlcodegen;

/** Logger abstraction. */
public interface Logger {

  /**
   * Logs an info message.
   *
   * @param format the format string
   * @param args the arguments
   */
  void info(String format, Object... args);

  /**
   * Logs a debug message.
   *
   * @param format the format string
   * @param args the arguments
   */
  void debug(String format, Object... args);

  /**
   * Logs a warning message.
   *
   * @param format the format string
   * @param args the arguments
   */
  void warn(String format, Object... args);

  /**
   * Logs an error message.
   *
   * @param format the format string
   * @param args the arguments
   */
  void error(String format, Object... args);
}
