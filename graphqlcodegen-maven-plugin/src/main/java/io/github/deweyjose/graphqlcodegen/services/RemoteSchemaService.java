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

public class RemoteSchemaService {
  private final HttpClient httpClient;
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Builder
  @Getter
  public static class IntrospectionOperation {
    private String query;
    private String operationName;
  }

  public static class IntrospectionRequest {
    public IntrospectionOperation operation;
    public Map<String, Object> variables;
  }

  public RemoteSchemaService() {
    this.httpClient = HttpClient.newHttpClient();
  }

  public String getRemoteSchemaFile(String url) throws IOException, InterruptedException {
    HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    Logger.debug("Remote schema file: {}", response.body());
    if (response.statusCode() != 200) {
      throw new IOException("Failed to get remote schema file: " + response.statusCode());
    }
    return response.body();
  }

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
