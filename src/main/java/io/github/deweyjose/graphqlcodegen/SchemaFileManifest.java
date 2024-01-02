package io.github.deweyjose.graphqlcodegen;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

@Slf4j
public class SchemaFileManifest {
    private Set<File> files;
    private final Properties manifest;
    private final File manifestPath;

    /**
     * Manifest constructor loads the properties file into memory.
     * The properties file has a property for each path with the
     * previous known checksum.
     *
     * @param files
     * @param manifestPath
     */
    public SchemaFileManifest(Set<File> files, File manifestPath) {
        this.files = files;
        this.manifestPath = manifestPath;
        manifest = loadManifest(manifestPath);
    }

    public SchemaFileManifest(File manifestPath) {
        this.manifestPath = manifestPath;
        manifest = loadManifest(manifestPath);
    }

    @SneakyThrows
    public static Properties loadManifest(File manifestPath) {
        Properties properties = new Properties();
        if (manifestPath.exists()) {
            try (FileInputStream fis = new FileInputStream(manifestPath)) {
                properties.load(fis);
            }
        }
        return properties;
    }

    @SneakyThrows
    public static String generateChecksum(File path) {
        byte[] data = Files.readAllBytes(Paths.get(path.toURI()));
        byte[] hash = MessageDigest.getInstance("MD5").digest(data);
        String checksum = new BigInteger(1, hash).toString(16);
        return checksum;
    }

    /**
     * We only care about files ending with .graphql(s)
     *
     * @param file
     * @return boolean
     */
    public static boolean isGraphqlFile(File file) {
        return file.getName().endsWith(".graphqls") ||
            file.getName().endsWith(".graphql");
    }

    /**
     * Traverse the directory structure collecting .graphql(s) files.
     *
     * @param directory
     * @return Set
     */
    public static Set<File> findGraphQLSFiles(File directory) {
        Set<File> result = new HashSet<>();

        File[] contents = directory.listFiles();
        if (contents != null) {
            for (File content : contents) {
                if (content.isFile() && isGraphqlFile(content)) {
                    result.add(content);
                } else if (content.isDirectory()) {
                    Set<File> subdirectoryGraphQLSFiles = findGraphQLSFiles(content);
                    result.addAll(subdirectoryGraphQLSFiles);
                }
            }
        }

        return result;
    }

    public void setFiles(Set<File> files) {
        this.files = files;
    }

    /**
     * Computes the Set of files that have changed or are new
     * and need to trigger code generation.
     *
     * @return Set
     */
    public Set<File> getChangedFiles() {
        Set<File> changed = new HashSet<>();
        for (File file : files) {
            String oldChecksum = manifest.getProperty(file.getPath());
            if (oldChecksum == null) {
                log.info("{} is new, will generate code", file.getName());
            } else if (!oldChecksum.equals(generateChecksum(file))) {
                log.info("{} has changed, will generate code", file.getName());
            } else {
                log.info("{} has not changed, will not generate code", file.getName());
                continue;
            }
            changed.add(file);
        }
        return changed;
    }

    /**
     * Clear the old manifest, compute new checksums
     * for each file and save the properties file.
     */
    @SneakyThrows
    public void syncManifest() {
        manifest.clear();
        for (File file : files) {
            manifest.put(file.getPath(), generateChecksum(file));
        }

        try (FileOutputStream fos = new FileOutputStream(manifestPath)) {
            manifest.store(fos, "Schema Manifest");
            fos.flush();
        }
    }
}
