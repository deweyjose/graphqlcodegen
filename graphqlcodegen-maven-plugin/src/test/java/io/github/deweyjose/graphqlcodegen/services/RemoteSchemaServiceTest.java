package io.github.deweyjose.graphqlcodegen.services;

import static org.junit.jupiter.api.Assertions.*;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import io.github.deweyjose.graphqlcodegen.TestUtils;
import io.github.deweyjose.graphqlcodegen.services.RemoteSchemaService.IntrospectionOperation;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class RemoteSchemaServiceTest {
  private static HttpServer server;
  private static String baseUrl;
  private static final String GET_RESPONSE = "schema { query: Query }";
  private static final String INTROSPECTION_RESPONSE =
      TestUtils.getFileContent("introspection/query.response");
  private static final String INTROSPECTION_QUERY =
      TestUtils.getFileContent("introspection/query.graphqls");

  @BeforeAll
  static void startServer() throws IOException {
    server = HttpServer.create(new InetSocketAddress(0), 0);
    server.createContext(
        "/schema",
        new HttpHandler() {
          @Override
          public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
              byte[] resp = GET_RESPONSE.getBytes();
              exchange.sendResponseHeaders(200, resp.length);
              try (OutputStream os = exchange.getResponseBody()) {
                os.write(resp);
              }
            } else {
              exchange.sendResponseHeaders(405, -1);
            }
          }
        });
    server.createContext(
        "/introspect",
        new HttpHandler() {
          @Override
          public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
              byte[] resp = INTROSPECTION_RESPONSE.getBytes();
              exchange.sendResponseHeaders(200, resp.length);
              try (OutputStream os = exchange.getResponseBody()) {
                os.write(resp);
              }
            } else {
              exchange.sendResponseHeaders(405, -1);
            }
          }
        });
    server.createContext(
        "/notfound",
        exchange -> {
          exchange.sendResponseHeaders(404, -1);
        });
    server.start();
    baseUrl = "http://localhost:" + server.getAddress().getPort();
  }

  @AfterAll
  static void stopServer() {
    server.stop(0);
  }

  @Test
  void testGetRemoteSchemaFile_success() throws Exception {
    RemoteSchemaService service = new RemoteSchemaService();
    String result = service.getRemoteSchemaFile(baseUrl + "/schema");
    assertEquals(GET_RESPONSE, result);
  }

  @Test
  void testGetIntrospectionResults_success() throws Exception {
    RemoteSchemaService service = new RemoteSchemaService();
    String query = INTROSPECTION_QUERY;
    Map<String, String> headers = new HashMap<>();
    headers.put("X-Test-Header", "test");
    IntrospectionOperation operation =
        IntrospectionOperation.builder().query(query).operationName("IntrospectionQuery").build();
    String result = service.getIntrospectedSchemaFile(baseUrl + "/introspect", operation, headers);
    assertTrue(result.contains("type Query"), "Should contain type Query");
    assertTrue(result.contains("type Mutation"), "Should contain type Mutation");
    assertTrue(result.contains("type Subscription"), "Should contain type Subscription");
    assertTrue(result.contains("type Actor"), "Should contain type Actor");
    assertTrue(result.contains("type Comment"), "Should contain type Comment");
    assertTrue(result.contains("type Show"), "Should contain type Show");
    assertTrue(result.contains("type _Service"), "Should contain type _Service");
    assertTrue(result.contains("enum ErrorDetail"), "Should contain enum ErrorDetail");
    assertTrue(result.contains("enum ErrorType"), "Should contain enum ErrorType");
    assertTrue(result.contains("input ShowInput"), "Should contain input ShowInput");
    assertTrue(result.contains("scalar _FieldSet"), "Should contain scalar _FieldSet");
    assertTrue(result.contains("directive @deprecated"), "Should contain directive @deprecated");
    assertTrue(result.contains("directive @skip"), "Should contain directive @skip");
    assertTrue(result.contains("directive @include"), "Should contain directive @include");
  }

  @Test
  void testGetRemoteSchemaFile_notFound() {
    RemoteSchemaService service = new RemoteSchemaService();
    Exception ex =
        assertThrows(
            IOException.class,
            () -> {
              service.getRemoteSchemaFile(baseUrl + "/notfound");
            });
    assertTrue(ex.getMessage().contains("404"));
  }
}
