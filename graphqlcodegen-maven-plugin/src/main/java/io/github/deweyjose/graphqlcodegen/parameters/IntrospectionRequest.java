package io.github.deweyjose.graphqlcodegen.parameters;

import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IntrospectionRequest {
  private String url;
  private String query;
  private String operationName;
  private Map<String, String> headers;

  public IntrospectionRequest() {}
}
