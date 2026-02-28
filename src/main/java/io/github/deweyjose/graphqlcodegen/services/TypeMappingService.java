package io.github.deweyjose.graphqlcodegen.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import org.apache.maven.artifact.Artifact;

/** This class provides services related to type mapping. */
public class TypeMappingService {
  /**
   * Loads type mapping properties from the specified files inside a JAR file.
   *
   * @param artifactFile the JAR file to load properties from
   * @param typeMappingPropertiesFiles the list of property file paths inside the JAR
   * @return a map containing the type mappings loaded from the properties files
   */
  public Map<String, String> loadPropertiesFile(
      File artifactFile, List<String> typeMappingPropertiesFiles) {
    Map<String, String> typeMapping = new HashMap<>();
    try (JarFile jarFile = new JarFile(artifactFile)) {
      for (String file : typeMappingPropertiesFiles) {
        ZipEntry entry = jarFile.getEntry(file);
        if (entry != null) {
          try (InputStream inputStream = jarFile.getInputStream(entry)) {
            Properties typeMappingProperties = new Properties();
            typeMappingProperties.load(inputStream);
            typeMappingProperties.forEach(
                (k, v) -> typeMapping.putIfAbsent(String.valueOf(k), String.valueOf(v)));
          }
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return typeMapping;
  }

  /**
   * Loads type mapping properties from local files.
   *
   * @param projectBaseDir the project base directory
   * @param localTypeMappingPropertiesFiles the list of local property file paths
   * @return a map containing the type mappings loaded from the local properties files
   */
  public Map<String, String> loadLocalPropertiesFiles(
      File projectBaseDir, List<String> localTypeMappingPropertiesFiles) {
    Map<String, String> typeMapping = new HashMap<>();
    if (localTypeMappingPropertiesFiles != null && !localTypeMappingPropertiesFiles.isEmpty()) {
      for (String file : localTypeMappingPropertiesFiles) {
        File propertiesFile = new File(projectBaseDir, file);
        if (propertiesFile.exists() && propertiesFile.isFile()) {
          try (FileInputStream inputStream = new FileInputStream(propertiesFile)) {
            Properties typeMappingProperties = new Properties();
            typeMappingProperties.load(inputStream);
            typeMappingProperties.forEach(
                (k, v) -> typeMapping.putIfAbsent(String.valueOf(k), String.valueOf(v)));
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      }
    }
    return typeMapping;
  }

  /**
   * Merges user-provided type mappings with those loaded from properties files in dependency
   * artifacts and local files.
   *
   * @param userTypeMapping the user-provided type mapping (may be null)
   * @param typeMappingPropertiesFiles the list of property file paths to search for in dependencies
   * @param localTypeMappingPropertiesFiles the list of local property file paths
   * @param artifacts the set of Maven dependency artifacts to search for property files
   * @param projectBaseDir the project base directory
   * @return a map containing the merged type mappings, with user mappings taking precedence
   */
  public Map<String, String> mergeTypeMapping(
      Map<String, String> userTypeMapping,
      List<String> typeMappingPropertiesFiles,
      List<String> localTypeMappingPropertiesFiles,
      Set<Artifact> artifacts,
      File projectBaseDir) {
    Map<String, String> jarTypeMapping = new HashMap<>();
    if (typeMappingPropertiesFiles != null && !typeMappingPropertiesFiles.isEmpty()) {
      for (Artifact dependency : artifacts) {
        File artifactFile = dependency.getFile();
        if (artifactFile != null && artifactFile.isFile()) {
          jarTypeMapping.putAll(loadPropertiesFile(artifactFile, typeMappingPropertiesFiles));
        }
      }
    }

    // Load local type mappings
    Map<String, String> localTypeMapping =
        loadLocalPropertiesFiles(projectBaseDir, localTypeMappingPropertiesFiles);

    // Merge all mappings with user mappings taking precedence
    Map<String, String> finalTypeMapping = new HashMap<>(jarTypeMapping);
    finalTypeMapping.putAll(localTypeMapping);
    if (userTypeMapping != null) {
      finalTypeMapping.putAll(userTypeMapping);
    }
    return finalTypeMapping;
  }
}
