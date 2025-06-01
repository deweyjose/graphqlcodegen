package io.github.deweyjose.graphqlcodegen.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.ExecutionResult;
import graphql.introspection.IntrospectionResultToSchema;
import graphql.language.Document;
import graphql.schema.idl.SchemaPrinter;
import io.github.deweyjose.graphqlcodegen.Logger;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.SneakyThrows;

/**
 * Service for fetching and converting remote GraphQL schemas, including introspection queries.
 *
 * <p>This service provides methods to:
 *
 * <ul>
 *   <li>Fetch a remote GraphQL schema file via HTTP GET
 *   <li>Fetch and convert a remote GraphQL schema via introspection (HTTP POST)
 *   <li>Convert introspection JSON results to GraphQL SDL
 * </ul>
 */
public class RemoteSchemaService {
  private final HttpClient httpClient;
  private final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * Represents a GraphQL introspection operation, including the query and optional operation name.
   */
  @Builder
  @Getter
  public static class IntrospectionOperation {
    /** The GraphQL introspection query string. */
    private String query;

    /** The operation name for the introspection query (optional). */
    private String operationName;
  }

  /** Constructs a new RemoteSchemaService using Java's built-in HttpClient. */
  public RemoteSchemaService() {
    this.httpClient = HttpClient.newHttpClient();
  }

  /**
   * Fetches a remote GraphQL schema file via HTTP GET.
   *
   * @param url the URL of the remote schema file
   * @return the contents of the schema file as a String
   * @throws IOException if the request fails or returns a non-200 status
   * @throws InterruptedException if the thread is interrupted
   */
  public String getRemoteSchemaFile(String url) throws IOException, InterruptedException {
    HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    Logger.debug("Remote schema file: {}", response.body());
    if (response.statusCode() != 200) {
      throw new IOException("Failed to get remote schema file: " + response.statusCode());
    }
    return response.body();
  }

  /**
   * Fetches a remote GraphQL schema via introspection (HTTP POST) and converts it to SDL.
   *
   * @param url the URL of the GraphQL endpoint
   * @param operation the introspection operation (query and operation name)
   * @param headers additional HTTP headers to include in the request
   * @return the GraphQL schema SDL as a String
   * @throws IOException if the request fails or returns a non-200 status
   */
  @SneakyThrows
  public String getIntrospectedSchemaFile(
      String url, IntrospectionOperation operation, Map<String, String> headers) {
    HttpRequest.Builder builder =
        HttpRequest.newBuilder()
            .uri(URI.create(url))
            .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(operation)))
            .header("Content-Type", "application/json");

    if (!headers.isEmpty()) {
      headers.forEach(builder::header);
    }

    HttpRequest request = builder.build();
    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    Logger.debug("Introspection results: {}", response.body());
    if (response.statusCode() != 200) {
      throw new IOException("Failed to get introspection results: " + response.statusCode());
    }
    Map<String, Object> introspection =
        objectMapper.readValue(response.body(), new TypeReference<Map<String, Object>>() {});
    return convertIntrospectionToSchema(introspection);
  }

  /**
   * Converts a parsed introspection result (as a Map) to GraphQL SDL.
   *
   * @param introspection the introspection result as a Map (should contain a "data" key)
   * @return the GraphQL schema SDL as a String
   */
  public String convertIntrospectionToSchema(Map<String, Object> introspection) {
    IntrospectionResultToSchema introspectionResultToSchema = new IntrospectionResultToSchema();
    ExecutionResult executionResult =
        ExecutionResult.newExecutionResult().data(introspection.get("data")).build();
    Document schema = introspectionResultToSchema.createSchemaDefinition(executionResult);
    SchemaPrinter schemaPrinter = new SchemaPrinter();
    String sdl = schemaPrinter.print(schema);
    return sdl;
  }
}
