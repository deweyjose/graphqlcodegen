package io.github.deweyjose.graphqlcodegen;

import static java.lang.String.format;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;

// @@@ TODO: unit tests

public class DependencySchemaExtractor {
  public static List<File> extract(MavenProject project, String[] schemaJarFilesFromDependencies) {
    List<File> files = new ArrayList<>();

    for (final String jarDep : schemaJarFilesFromDependencies) {
      final String jarDepClean = jarDep.trim();
      if (jarDepClean.isEmpty()) {
        continue;
      }

      final Optional<Artifact> artifactOpt = findFromDependencies(project, jarDepClean);
      if (artifactOpt.isPresent()) {
        final Artifact artifact = artifactOpt.get();
        final File file = artifact.getFile();
        files.add(file);
      }
    }

    return files;
  }

  private static Optional<Artifact> findFromDependencies(
      MavenProject project, final String artifactRef) {
    final String cleanRef = artifactRef.trim();
    final Set<Artifact> dependencyArtifacts = project.getDependencyArtifacts();

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
