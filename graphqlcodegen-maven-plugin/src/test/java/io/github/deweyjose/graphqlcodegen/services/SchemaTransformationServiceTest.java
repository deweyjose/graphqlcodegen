package io.github.deweyjose.graphqlcodegen.services;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SchemaTransformationServiceTest {

  private final SchemaTransformationService service = new SchemaTransformationService();

  @Test
  void shouldTransformCustomRootTypes() {
    // Given
    String schema =
        """
            schema {
                query: CustomQuery
                mutation: CustomMutation
                subscription: CustomSubscription
            }

            type CustomQuery {
                hello: String
            }

            type CustomMutation {
                updateHello(message: String): String
            }

            type CustomSubscription {
                helloEvents: String
            }
            """;

    // When
    String transformed = service.transformSchema(schema);

    // Then
    assertTrue(transformed.contains("type Query {"));
    assertTrue(transformed.contains("type Mutation {"));
    assertTrue(transformed.contains("type Subscription {"));
    assertTrue(transformed.contains("hello: String"));
    assertTrue(transformed.contains("updateHello(message: String): String"));
    assertTrue(transformed.contains("helloEvents: String"));
  }

  @Test
  void shouldPreserveTypeDefinitions() {
    // Given
    String schema =
        """
            schema {
                query: CustomQuery
            }

            type CustomQuery {
                hello: String
                user: User
            }

            type User {
                id: ID!
                name: String!
            }
            """;

    // When
    String transformed = service.transformSchema(schema);

    // Then
    assertTrue(transformed.contains("type Query {"));
    assertTrue(transformed.contains("hello: String"));
    assertTrue(transformed.contains("user: User"));
    assertTrue(transformed.contains("type User {"));
    assertTrue(transformed.contains("id: ID!"));
    assertTrue(transformed.contains("name: String!"));
  }

  @Test
  void shouldHandleTypeExtensions() {
    // Given
    String schema =
        """
            schema {
                query: CustomQuery
            }

            type CustomQuery {
                hello: String
            }

            extend type CustomQuery {
                user: User
            }

            type User {
                id: ID!
                name: String!
            }
            """;

    // When
    String transformed = service.transformSchema(schema);
    // Then
    assertTrue(transformed.contains("type Query {"));
    assertTrue(transformed.contains("hello: String"));
    assertTrue(transformed.contains("user: User"));
    assertTrue(transformed.contains("type User {"));
    assertTrue(transformed.contains("id: ID!"));
    assertTrue(transformed.contains("name: String!"));
  }

  @Test
  void shouldHandleSchemaWithoutCustomRootTypes() {
    // Given
    String schema =
        """
            type User {
                id: ID!
                name: String!
            }
            """;

    // When
    String transformed = service.transformSchema(schema);

    // Then
    assertEquals(schema, transformed);
  }

  @Test
  void shouldTransformSchemaFile(@TempDir Path tempDir) throws IOException {
    // Given
    Path schemaFile = tempDir.resolve("schema.graphql");
    String schema =
        """
            schema {
                query: CustomQuery
            }

            type CustomQuery {
                hello: String
            }
            """;
    Files.writeString(schemaFile, schema);

    // When
    String transformed = service.transformSchemaFile(schemaFile);

    // Then
    assertTrue(transformed.contains("type Query {"));
    assertTrue(transformed.contains("hello: String"));
    assertTrue(Files.readString(schemaFile).contains("type Query {"));
  }
}
