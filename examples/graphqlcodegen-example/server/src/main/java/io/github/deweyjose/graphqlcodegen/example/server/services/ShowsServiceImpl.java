package io.github.deweyjose.graphqlcodegen.example.server.services;

import com.acme.types.ShowInput;
import graphql.com.google.common.collect.Lists;
import io.github.deweyjose.graphqlcodegen.example.common.Show;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ShowsServiceImpl implements ShowsService {

  private List<Show> shows =
      Lists.newArrayList(
          Show.builder().id(1).title("Stranger Things").releaseYear(2016).build(),
          Show.builder().id(2).title("Ozark").releaseYear(2017).build(),
          Show.builder().id(3).title("The Crown").releaseYear(2016).build(),
          Show.builder().id(4).title("Dead to Me").releaseYear(2019).build(),
          Show.builder().id(5).title("Orange is the New Black").releaseYear(2013).build());

  @Override
  public List<Show> shows() {
    return shows;
  }

  @Override
  public Show add(ShowInput input) {
    Show show =
        Show.builder()
            .id(shows.size() + 1)
            .title(input.getTitle())
            .releaseYear(input.getReleaseYear())
            .build();

    shows.add(show);

    return show;
  }
}
