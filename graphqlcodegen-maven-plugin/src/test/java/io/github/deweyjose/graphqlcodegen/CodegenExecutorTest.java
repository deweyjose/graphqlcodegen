package io.github.deweyjose.graphqlcodegen;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class CodegenExecutorTest {

  @Test
  void testToSet_nullAndEmptyAndNormal() {
    assertEquals(Collections.emptySet(), CodegenExecutor.toSet(null));
    assertEquals(Collections.emptySet(), CodegenExecutor.toSet(new String[0]));
    assertEquals(Set.of("a", "b"), CodegenExecutor.toSet(new String[] {"a", "b"}));
  }

  @Test
  void testToMap_nullAndEmptyAndNormal() {
    assertEquals(Collections.emptyMap(), CodegenExecutor.toMap(null));
    assertEquals(Collections.emptyMap(), CodegenExecutor.toMap(Collections.emptyMap()));
    ParameterMap paramMap = new ParameterMap();
    Map<String, String> props = new HashMap<>();
    props.put("foo", "bar");
    paramMap.setProperties(props);
    Map<String, ParameterMap> input = new HashMap<>();
    input.put("key", paramMap);
    Map<String, Map<String, String>> result = CodegenExecutor.toMap(input);
    assertEquals(1, result.size());
    assertEquals(Map.of("foo", "bar"), result.get("key"));
  }
}
