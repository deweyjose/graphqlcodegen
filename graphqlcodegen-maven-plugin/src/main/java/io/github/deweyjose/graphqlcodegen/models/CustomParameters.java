package io.github.deweyjose.graphqlcodegen.models;

import java.io.File;
import org.apache.maven.project.MavenProject;

public record CustomParameters(
    File[] schemaPaths,
    String[] schemaJarFilesFromDependencies,
    File schemaManifestOutputDir,
    boolean onlyGenerateChanged,
    MavenProject project,
    String[] typeMappingPropertiesFiles) {}
