package io.github.deweyjose.graphqlcodegen.example.server.datafetchers;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsQuery;
import io.github.deweyjose.graphqlcodegen.example.server.model.Theater;
import io.github.deweyjose.graphqlcodegen.example.server.services.TheaterService;
import java.util.List;

@DgsComponent
public class TheaterDatafetcher {
  private final TheaterService theaterService;

  public TheaterDatafetcher(TheaterService theaterService) {
    this.theaterService = theaterService;
  }

  @DgsQuery
  public List<Theater> theaters() {
    return theaterService.theaters();
  }
}
