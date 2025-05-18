package io.github.deweyjose.graphqlcodegen.models;

import java.io.File;

public record CustomParameters(
    File[] schemaPaths,
    String[] schemaJarFilesFromDependencies,
    File schemaManifestOutputDir,
    boolean onlyGenerateChanged,
    File baseDir,
    String[] typeMappingPropertiesFiles) {}
