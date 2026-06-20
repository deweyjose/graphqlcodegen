package io.github.deweyjose.graphqlcodegen.parameters;

import java.util.Map;
import org.apache.maven.plugins.annotations.Parameter;

/** Wrapper for a map of string properties, used for plugin parameter mapping. */
public class ParameterMap {
  @Parameter private Map<String, String> properties;

  /**
   * Returns the string representation of the properties map.
   *
   * @return the properties as a string
   */
  @Override
  public String toString() {
    return properties.toString();
  } // to test

  /**
   * Gets the properties map.
   *
   * @return the properties map
   */
  public Map<String, String> getProperties() {
    return properties;
  }

  /**
   * Sets the properties map.
   *
   * @param properties the properties map
   */
  public void setProperties(final Map<String, String> properties) {
    this.properties = properties;
  }
}
