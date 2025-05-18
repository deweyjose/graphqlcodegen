package io.github.deweyjose.graphqlcodegen;

import static java.lang.String.format;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.apache.maven.artifact.Artifact;

public class DependencySchemaExtractor {

  /**
   * Extracts the schema files from the dependencies.
   *
   * @param dependencyArtifacts the set of dependency artifacts
   * @param schemaJarFilesFromDependencies the schema jar files from dependencies
   * @return the schema files
   */
  public static List<File> extract(
      Set<Artifact> dependencyArtifacts, String[] schemaJarFilesFromDependencies) {
    List<File> files = new ArrayList<>();

    for (final String jarDep : schemaJarFilesFromDependencies) {
      final String jarDepClean = jarDep.trim();
      if (jarDepClean.isEmpty()) {
        continue;
      }

      final Optional<Artifact> artifactOpt = findFromDependencies(dependencyArtifacts, jarDepClean);
      if (artifactOpt.isPresent()) {
        final Artifact artifact = artifactOpt.get();
        final File file = artifact.getFile();
        files.add(file);
      }
    }

    return files;
  }

  /**
   * Finds the artifact from the dependencies.
   *
   * @param dependencyArtifacts the set of dependency artifacts
   * @param artifactRef the artifact reference
   * @return the artifact
   */
  private static Optional<Artifact> findFromDependencies(
      Set<Artifact> dependencyArtifacts, final String artifactRef) {
    final String cleanRef = artifactRef.trim();

    for (final Artifact artifact : dependencyArtifacts) {
      final String ref =
          format(
              "%s:%s:%s", artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion());

      if (ref.equals(cleanRef)) {
        return Optional.of(artifact);
      }
    }
    return Optional.empty();
  }
}
