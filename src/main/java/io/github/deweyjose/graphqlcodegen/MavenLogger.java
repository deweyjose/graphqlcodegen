package io.github.deweyjose.graphqlcodegen;

import java.util.Objects;
import org.apache.maven.plugin.logging.Log;
import org.slf4j.helpers.MessageFormatter;

/** Logger class for logging messages to the Maven logger. */
class MavenLogger implements Logger {
  private final Log mavenLog;

  MavenLogger(Log log) {
    mavenLog = Objects.requireNonNull(log);
  }

  @Override
  public void info(String format, Object... args) {
    if (mavenLog.isInfoEnabled()) {
      mavenLog.info(MessageFormatter.arrayFormat(format, args).getMessage());
    }
  }

  @Override
  public void debug(String format, Object... args) {
    if (mavenLog.isDebugEnabled()) {
      mavenLog.debug(MessageFormatter.arrayFormat(format, args).getMessage());
    }
  }

  @Override
  public void warn(String format, Object... args) {
    if (mavenLog.isWarnEnabled()) {
      mavenLog.warn(MessageFormatter.arrayFormat(format, args).getMessage());
    }
  }

  @Override
  public void error(String format, Object... args) {
    if (mavenLog.isErrorEnabled()) {
      mavenLog.error(MessageFormatter.arrayFormat(format, args).getMessage());
    }
  }
}
