package io.github.deweyjose.graphqlcodegen;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.maven.plugin.logging.Log;
import org.junit.jupiter.api.Test;

class MavenLoggerTest {

  @Test
  void delegatesSlf4jFormattedMessagesWhenLevelEnabled() {
    Log log = mock(Log.class);
    when(log.isInfoEnabled()).thenReturn(true);
    when(log.isDebugEnabled()).thenReturn(true);
    when(log.isWarnEnabled()).thenReturn(true);
    when(log.isErrorEnabled()).thenReturn(true);

    Logger logger = new MavenLogger(log);
    logger.info("info {}", "a");
    logger.debug("debug {} {}", "b", 2);
    logger.warn("warn {}", "c");
    logger.error("error {}", "d");

    // {} placeholders are substituted before reaching the Maven log.
    verify(log).info("info a");
    verify(log).debug("debug b 2");
    verify(log).warn("warn c");
    verify(log).error("error d");
  }

  @Test
  void skipsLoggingWhenLevelDisabled() {
    Log log = mock(Log.class);
    when(log.isInfoEnabled()).thenReturn(false);
    when(log.isDebugEnabled()).thenReturn(false);
    when(log.isWarnEnabled()).thenReturn(false);
    when(log.isErrorEnabled()).thenReturn(false);

    Logger logger = new MavenLogger(log);
    logger.info("info {}", "a");
    logger.debug("debug {}", "b");
    logger.warn("warn {}", "c");
    logger.error("error {}", "d");

    verify(log, never()).info(org.mockito.ArgumentMatchers.any(CharSequence.class));
    verify(log, never()).debug(org.mockito.ArgumentMatchers.any(CharSequence.class));
    verify(log, never()).warn(org.mockito.ArgumentMatchers.any(CharSequence.class));
    verify(log, never()).error(org.mockito.ArgumentMatchers.any(CharSequence.class));
  }

  @Test
  void requiresNonNullLog() {
    assertThrows(NullPointerException.class, () -> new MavenLogger(null));
  }
}
