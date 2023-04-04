package io.github.deweyjose.graphqlcodegen;

import org.apache.maven.plugins.annotations.Parameter;

import java.util.Map;

public class Properties {
  @Parameter
  private Map<String, String> properties;

  @Override
  public String toString() {
    return properties.toString();
  } // to test

  public Map<String, String> getProperties() {
    return properties;
  }

  public void setProperties(Map<String, String> properties) {
    this.properties = properties;
  }
}
