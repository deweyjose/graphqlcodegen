package io.github.deweyjose.graphqlcodegen.services;

import graphql.language.*;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/** Service for transforming GraphQL schemas. */
@Slf4j
public class SchemaTransformationService {
  private static final String QUERY = "Query";
  private static final String MUTATION = "Mutation";
  private static final String SUBSCRIPTION = "Subscription";

  /**
   * Transforms GraphQL schema content by normalizing root operation type names.
   *
   * @param schemaContent the schema content
   * @return the transformed schema
   */
  @SneakyThrows
  public String transformSchema(String schemaContent) {
    TypeDefinitionRegistry registry = new SchemaParser().parse(schemaContent);
    Optional<SchemaDefinition> schemaDefOpt = registry.schemaDefinition();

    if (schemaDefOpt.isEmpty()) {
      log.debug("No schema definition found, skipping transformation");
      return schemaContent;
    }

    SchemaDefinition schemaDef = schemaDefOpt.get();
    Map<String, String> typeMappings = extractRootTypeMappings(schemaDef);

    if (typeMappings.isEmpty()) {
      log.debug("No custom root types found, skipping transformation");
      return schemaContent;
    }

    typeMappings.forEach((oldName, newName) -> renameTypeAndExtensions(registry, oldName, newName));
    SchemaDefinition newSchemaDef = rebuildSchemaDefinition(schemaDef, typeMappings);

    registry.remove(schemaDef);
    registry.add(newSchemaDef);

    return printSchema(registry);
  }

  /**
   * Reads and transforms a schema file in place.
   *
   * @param schemaFile the schema file path
   * @return the transformed schema content
   * @throws IOException if reading or writing fails
   */
  public String transformSchemaFile(Path schemaFile) throws IOException {
    String content = Files.readString(schemaFile);
    String transformed = transformSchema(content);
    if (!content.equals(transformed)) {
      log.info("Transformed schema file: {}", schemaFile);
      Files.writeString(schemaFile, transformed);
    }
    return transformed;
  }

  private Map<String, String> extractRootTypeMappings(SchemaDefinition schemaDef) {
    Map<String, String> mappings = new HashMap<>();
    for (OperationTypeDefinition op : schemaDef.getOperationTypeDefinitions()) {
      String typeName = op.getTypeName().getName();
      switch (op.getName()) {
        case "query" -> mappings.put(typeName, QUERY);
        case "mutation" -> mappings.put(typeName, MUTATION);
        case "subscription" -> mappings.put(typeName, SUBSCRIPTION);
      }
    }
    return mappings;
  }

  private void renameTypeAndExtensions(
      TypeDefinitionRegistry registry, String oldName, String newName) {
    registry
        .getType(oldName)
        .ifPresent(
            type -> {
              if (type instanceof ObjectTypeDefinition objType) {
                registry.remove(type);
                registry.add(objType.transform(builder -> builder.name(newName)));
              }
            });

    List<ObjectTypeExtensionDefinition> extensions =
        new ArrayList<>(registry.objectTypeExtensions().getOrDefault(oldName, List.of()));
    for (ObjectTypeExtensionDefinition ext : extensions) {
      registry.remove(ext);
      registry.add(ext.transformExtension(builder -> builder.name(newName)));
    }
  }

  private SchemaDefinition rebuildSchemaDefinition(
      SchemaDefinition original, Map<String, String> typeMappings) {
    List<OperationTypeDefinition> newOps = new ArrayList<>();
    for (OperationTypeDefinition op : original.getOperationTypeDefinitions()) {
      String updatedName =
          typeMappings.getOrDefault(op.getTypeName().getName(), op.getTypeName().getName());
      newOps.add(
          op.transform(
              builder -> builder.typeName(TypeName.newTypeName().name(updatedName).build())));
    }
    return original.transform(builder -> builder.operationTypeDefinitions(newOps));
  }

  private String printSchema(TypeDefinitionRegistry registry) {
    GraphQLSchema schema =
        new SchemaGenerator()
            .makeExecutableSchema(registry, RuntimeWiring.newRuntimeWiring().build());
    return new SchemaPrinter().print(schema);
  }
}
