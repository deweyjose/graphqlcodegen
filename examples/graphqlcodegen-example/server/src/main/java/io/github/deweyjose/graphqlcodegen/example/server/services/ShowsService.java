package io.github.deweyjose.graphqlcodegen.example.server.services;

import com.acme.types.ShowInput;
import io.github.deweyjose.graphqlcodegen.example.common.Show;
import java.util.List;

public interface ShowsService {
  List<Show> shows();

  Show add(ShowInput input);
}
