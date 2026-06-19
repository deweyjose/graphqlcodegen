package io.github.deweyjose.graphqlcodegen;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.helpers.MessageFormatter;

@Slf4j
public class Slf4jLogger implements Logger {

  @Override
  public void info(String format, Object... args) {
    if (log.isInfoEnabled()) {
      log.info(MessageFormatter.arrayFormat(format, args).getMessage());
    }
  }

  @Override
  public void debug(String format, Object... args) {
    if (log.isDebugEnabled()) {
      log.debug(MessageFormatter.arrayFormat(format, args).getMessage());
    }
  }

  @Override
  public void warn(String format, Object... args) {
    if (log.isWarnEnabled()) {
      log.warn(MessageFormatter.arrayFormat(format, args).getMessage());
    }
  }

  @Override
  public void error(String format, Object... args) {
    if (log.isErrorEnabled()) {
      log.error(MessageFormatter.arrayFormat(format, args).getMessage());
    }
  }
}
