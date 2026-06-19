package io.github.deweyjose.graphqlcodegen.example.server.model;

import lombok.Data;

@Data
public class Theater {
  private String id;
  private String name;
  private String location;

  public Theater() {}

  public Theater(String id, String name, String location) {
    this.id = id;
    this.name = name;
    this.location = location;
  }
}
