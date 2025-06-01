package io.github.deweyjose.graphqlcodegen;

import org.apache.maven.plugin.logging.Log;

public class TestLog implements Log {
  @Override
  public void debug(CharSequence content) {}

  @Override
  public void debug(CharSequence content, Throwable error) {}

  @Override
  public void debug(Throwable error) {}

  @Override
  public void info(CharSequence content) {}

  @Override
  public void info(CharSequence content, Throwable error) {}

  @Override
  public void info(Throwable error) {}

  @Override
  public void warn(CharSequence content) {}

  @Override
  public void warn(CharSequence content, Throwable error) {}

  @Override
  public void warn(Throwable error) {}

  @Override
  public void error(CharSequence content) {}

  @Override
  public void error(CharSequence content, Throwable error) {}

  @Override
  public void error(Throwable error) {}

  @Override
  public boolean isDebugEnabled() {
    return false;
  }

  @Override
  public boolean isInfoEnabled() {
    return false;
  }

  @Override
  public boolean isWarnEnabled() {
    return false;
  }

  @Override
  public boolean isErrorEnabled() {
    return false;
  }
}
