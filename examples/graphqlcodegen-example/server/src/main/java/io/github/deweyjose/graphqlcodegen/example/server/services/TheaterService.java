package io.github.deweyjose.graphqlcodegen.example.server.services;

import io.github.deweyjose.graphqlcodegen.example.server.model.Theater;
import java.util.List;

public interface TheaterService {
  List<Theater> theaters();
}
