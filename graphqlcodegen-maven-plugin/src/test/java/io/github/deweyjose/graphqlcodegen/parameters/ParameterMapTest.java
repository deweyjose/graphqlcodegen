package io.github.deweyjose.graphqlcodegen.parameters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ParameterMapTest {

  @Test
  void exposesPropertiesViaAccessors() {
    Map<String, String> props = new LinkedHashMap<>();
    props.put("Locale", "java.util.Locale");

    ParameterMap map = new ParameterMap();
    map.setProperties(props);

    assertSame(props, map.getProperties());
  }

  @Test
  void toStringReflectsProperties() {
    Map<String, String> props = new LinkedHashMap<>();
    props.put("Show", "com.example.Show");
    props.put("Locale", "java.util.Locale");

    ParameterMap map = new ParameterMap();
    map.setProperties(props);

    assertEquals(props.toString(), map.toString());
  }
}
