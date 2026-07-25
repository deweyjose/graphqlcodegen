package io.github.deweyjose.graphqlcodegen.example.kotlin3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.deweyjose.graphqlcodegen.example.kotlin3.types.Show;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

class Jackson3DeserializationTest {

  private final JsonMapper mapper = JsonMapper.builder().build();

  @Test
  void deserializesGeneratedTypeThroughJackson3Builder() {
    Show show =
        mapper.readValue(
            "{\"__typename\":\"Show\",\"title\":\"Ozark\",\"releaseYear\":2017}", Show.class);

    assertEquals("Ozark", show.getTitle());
    assertEquals(2017, show.getReleaseYear());
  }

  @Test
  void absentFieldThrowsWhenAccessed() {
    Show show = mapper.readValue("{\"title\":\"Ozark\"}", Show.class);

    assertEquals("Ozark", show.getTitle());
    assertThrows(IllegalStateException.class, show::getReleaseYear);
  }
}
