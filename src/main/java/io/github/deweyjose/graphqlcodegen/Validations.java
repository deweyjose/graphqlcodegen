package io.github.deweyjose.graphqlcodegen;

import java.io.File;
import java.util.*;

import static java.util.Objects.isNull;

public class Validations {

    /**
     * Ensures the package name isn't null.
     *
     * @param name
     */
    public static void verifyPackageName(String name) {
        if (isNull(name)) {
            throw new IllegalArgumentException("Please specify a packageName");
        }
    }

    /**
     * We sort the input files by their paths to ensure a consistent order for processing.
     * <p>
     * We maintain two sets:
     * encounteredDirectories to keep track of encountered directories
     * encounteredPaths to keep track of encountered paths.
     * <p>
     * We iterate through the sorted files and perform the following checks:
     * Directory overlap: throw an exception if duplicate directories are configured.
     * Duplicate files: throw an exception if duplicate files are configured.
     * Ancestry in already added directories:
     * if files exist in configured directories or subdirectories of configured directories
     * throw an exception
     *
     * @param files
     */
    public static void verifySchemaPaths(Collection<File> files) {
        // Sort the input files to ensure consistent order
        List<File> sortedFiles = new ArrayList<>(files);
        sortedFiles.sort(Comparator.comparing(File::getPath));

        Set<File> encounteredDirectories = new HashSet<>();
        Set<String> encounteredPaths = new HashSet<>();

        for (File file : sortedFiles) {
            if (!file.exists()) {
                throw new IllegalArgumentException("Configured path %s does not exist" + file.getPath());
            }

            String path = file.getPath();

            // Check for directory overlap
            File parent = file.getParentFile();

            // Check for ancestry in already added directories
            while (parent != null) {
                if (encounteredDirectories.contains(parent)) {
                    throw new IllegalArgumentException(
                        String.format("Overlap configured: %s contains %s", parent.getPath(), file.getPath())
                    );
                }
                parent = parent.getParentFile();
            }

            // Update the encountered sets
            if (file.isDirectory()) {
                encounteredDirectories.add(file);
            }

            encounteredPaths.add(path);
        }
    }
}
