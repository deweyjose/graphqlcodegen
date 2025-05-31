package io.github.deweyjose.graphqlcodegen;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class SchemaFileService {
  public Set<File> expandSchemaPaths(File[] schemaPaths) {
    Set<File> configuredSchemaPaths =
        java.util.Arrays.stream(schemaPaths).collect(Collectors.toSet());
    Set<File> expandedSchemaPaths = new HashSet<>();
    for (File path : configuredSchemaPaths) {
      if (path.isFile()) {
        expandedSchemaPaths.add(path);
      } else {
        expandedSchemaPaths.addAll(SchemaManifestService.findGraphQLSFiles(path));
      }
    }
    return expandedSchemaPaths;
  }

  public void verifySchemaFiles(
      Set<File> fullSchemaPaths, String[] schemaJarFilesFromDependencies) {
    if (fullSchemaPaths.isEmpty()
        && (schemaJarFilesFromDependencies == null || schemaJarFilesFromDependencies.length < 1)) {
      throw new IllegalArgumentException("No schema files found. Please check your configuration.");
    }
  }

  public Set<File> filterChangedSchemaFiles(
      Set<File> allSchemaFiles, SchemaManifestService manifest) {
    manifest.setFiles(new HashSet<>(allSchemaFiles));
    Set<File> changed = new HashSet<>(allSchemaFiles);
    changed.retainAll(manifest.getChangedFiles());
    return changed;
  }

  public String fetchSchema(String url) throws IOException, InterruptedException {
    HttpClient client = HttpClient.newHttpClient();
    HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();
    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
    return response.body();
  }

  public File saveUrlToFile(String url, File outputDir) throws IOException, InterruptedException {
    String content = fetchSchema(url);
    String fileName = "remote-schemas/" + md5Hex(url) + ".graphqls";
    File outFile = new File(outputDir, fileName);
    Files.createDirectories(outFile.getParentFile().toPath());
    Files.writeString(outFile.toPath(), content);
    return outFile;
  }

  private String md5Hex(String input) {
    try {
      MessageDigest md = MessageDigest.getInstance("MD5");
      byte[] digest = md.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
      StringBuilder sb = new StringBuilder();
      for (byte b : digest) {
        sb.append(String.format("%02x", b));
      }
      return sb.toString();
    } catch (Exception e) {
      throw new RuntimeException("Failed to compute MD5 hash", e);
    }
  }
}
