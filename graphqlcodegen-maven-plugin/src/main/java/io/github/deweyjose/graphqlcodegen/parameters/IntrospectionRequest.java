package io.github.deweyjose.graphqlcodegen.parameters;

import java.util.Map;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents a request for introspection of a GraphQL schema.
 */
@Getter
@Setter
public class IntrospectionRequest {
  /**
   * The URL of the GraphQL endpoint to introspect.
   */
  private String url;

  /**
   * The query to use for the introspection.
   */
  private String query;
  /**
   * The operation name for the introspection query.
   */
  private String operationName;

  /**
   * Additional HTTP headers to include in the introspection request.
   */
  private Map<String, String> headers;

  /**
   * Constructs a new IntrospectionRequest.
   */
  public IntrospectionRequest() {}  
}
