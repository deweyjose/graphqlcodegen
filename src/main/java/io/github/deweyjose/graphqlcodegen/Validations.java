package io.github.deweyjose.graphqlcodegen;

import java.io.File;
import java.util.*;

public class Validations {

  /**
   * We sort the input files by their paths to ensure a consistent order for processing.
   *
   * We maintain three sets:
   *   validatedFiles to store the files that pass all checks,
   *   encounteredDirectories to keep track of encountered directories
   *   encounteredPaths to keep track of encountered paths.
   *
   * We iterate through the sorted files and perform the following checks:
   *   Directory overlap: Skip files that are in directories already encountered.
   *   Duplicate files: Skip files that are duplicates.
   *   Ancestry in already added directories: Skip files that have ancestry in directories already encountered.
   *
   * Files that pass all checks are added to the validatedFiles set, and the corresponding
   * directories and paths are updated in the encountered sets.
   * @param files
   */
  public static void verifySchemaPaths(Collection<File> files) {
    // Sort the input files to ensure consistent order
    List<File> sortedFiles = new ArrayList<>(files);
    sortedFiles.sort(Comparator.comparing(File::getPath));

    Set<File> encounteredDirectories = new HashSet<>();
    Set<String> encounteredPaths = new HashSet<>();

    for (File file : sortedFiles) {
      String path = file.getPath();

      // Check for directory overlap
      File parent = file.getParentFile();
      if (parent != null && encounteredDirectories.contains(parent)) {
        throw new IllegalArgumentException(
          String.format("Overlap detected: %s contains %s", parent.getPath(), file.getPath())
        );
      }

      // Check for duplicate files
      if (encounteredPaths.contains(path)) {
        continue; // Skip this file, it's a duplicate
      }

      // Check for ancestry in already added directories
      while (parent != null) {
        if (encounteredDirectories.contains(parent)) {
          throw new IllegalArgumentException(
            String.format("Overlap detected: %s contains %s", parent.getPath(), file.getPath())
          );
        }
        parent = parent.getParentFile();

        if (parent != null && parent.equals(file)) {
          break; // Avoid an infinite loop by checking if the parent is the same as the file
        }
      }

      // Update the encountered sets
      if (file.isDirectory()) {
        encounteredDirectories.add(file);
      }
      encounteredPaths.add(path);
    }
  }
}
