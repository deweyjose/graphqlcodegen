package io.github.deweyjose.graphqlcodegen;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ParameterMapTest {
  @Test
  void testToStringReturnsPropertiesString() {
    Map<String, String> props = new HashMap<>();
    props.put("foo", "bar");
    props.put("hello", "world");
    ParameterMap parameterMap = new ParameterMap();
    parameterMap.setProperties(props);
    String result = parameterMap.toString();
    assertTrue(result.contains("foo=bar"));
    assertTrue(result.contains("hello=world"));
    assertTrue(result.startsWith("{") && result.endsWith("}"));
  }
} 