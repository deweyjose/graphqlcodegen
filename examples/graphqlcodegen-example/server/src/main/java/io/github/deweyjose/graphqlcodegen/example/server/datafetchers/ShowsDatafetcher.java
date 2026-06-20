package io.github.deweyjose.graphqlcodegen.example.server.datafetchers;

import com.acme.types.ShowInput;
import com.netflix.graphql.dgs.*;
import io.github.deweyjose.graphqlcodegen.example.common.Show;
import io.github.deweyjose.graphqlcodegen.example.server.services.ShowsService;
import java.util.List;
import java.util.stream.Collectors;

@DgsComponent
public class ShowsDatafetcher {
  private final ShowsService showsService;

  public ShowsDatafetcher(ShowsService showsService) {
    this.showsService = showsService;
  }

  /**
   * This datafetcher resolves the shows field on Query. It uses an @InputArgument to get the
   * titleFilter from the Query if one is defined.
   */
  @DgsQuery
  public List<Show> shows(@InputArgument("titleFilter") String titleFilter) {
    if (titleFilter == null) {
      return showsService.shows();
    }

    return showsService.shows().stream()
        .filter(s -> s.getTitle().contains(titleFilter))
        .collect(Collectors.toList());
  }

  @DgsMutation
  public Show createShow(@InputArgument("showInput") ShowInput showInput) {
    return showsService.add(showInput);
  }
}
