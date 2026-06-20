package io.github.deweyjose.graphqlcodegen.example.server.services;

import io.github.deweyjose.graphqlcodegen.example.server.model.Theater;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class TheaterServiceImpl implements TheaterService {
  @Override
  public List<Theater> theaters() {
    return List.of(
        new Theater("1", "The Grand Theater", "123 Main St"),
        new Theater("2", "Cineplex 21", "456 Elm St"),
        new Theater("3", "Downtown Cinema", "789 Oak St"));
  }
}
