package io.github.deweyjose.graphqlcodegen.datafetchers;

import static org.assertj.core.api.Assertions.assertThat;

import com.netflix.graphql.dgs.DgsQueryExecutor;
import com.netflix.graphql.dgs.autoconfig.DgsAutoConfiguration;
import io.github.deweyjose.graphqlcodegen.example.common.Show;
import io.github.deweyjose.graphqlcodegen.example.server.datafetchers.ShowsDatafetcher;
import io.github.deweyjose.graphqlcodegen.example.server.services.ShowsServiceImpl;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
    classes = {ShowsDatafetcher.class, ShowsServiceImpl.class, DgsAutoConfiguration.class})
class ShowsDatafetcherTest {
  @Autowired DgsQueryExecutor dgsQueryExecutor;

  @Test
  public void testShows() {
    List<String> titles =
        dgsQueryExecutor.executeAndExtractJsonPath(
            " { shows { title releaseYear }}", "data.shows[*].title");

    assertThat(titles).contains("Ozark");
  }

  @Test
  public void testCreateShow() {
    Show show =
        dgsQueryExecutor.executeAndExtractJsonPathAsObject(
            "mutation { "
                + "  createShow(showInput:{title:\"foo\", releaseYear:1980}) {"
                + "    id title releaseYear"
                + "  }"
                + "}",
            "data.createShow",
            Show.class);
    Assertions.assertThat(show.getTitle()).isEqualTo("foo");
    Assertions.assertThat(show.getReleaseYear()).isEqualTo(1980);
    Assertions.assertThat(show.getId()).isNotNull();
  }
}
