package io.github.deweyjose.graphqlcodegen;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.netflix.graphql.dgs.codegen.CodeGen;
import com.netflix.graphql.dgs.codegen.CodeGenConfig;
import com.netflix.graphql.dgs.codegen.Language;

@Mojo(name = "generate", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class Codegen extends AbstractMojo {

    @Parameter(property = "schemaPaths", defaultValue = "${project.build.resources}/schema")
    private File[] schemaPaths;

    @Parameter(property = "packageName", defaultValue = "")
    private String packageName;

    @Parameter(property = "typeMapping")
    private Map typeMapping;

    @Parameter(property = "subPackageNameClient", defaultValue = "client")
    private String subPackageNameClient;

    @Parameter(property = "subPackageNameDatafetchers", defaultValue = "datafetchers")
    private String subPackageNameDatafetchers;

    @Parameter(property = "subPackageNameTypes", defaultValue = "types")
    private String subPackageNameTypes;

    @Parameter(property = "generateBoxedTypes", defaultValue = "false")
    private boolean generateBoxedTypes;

    @Parameter(property = "generateClient", defaultValue = "false")
    private boolean generateClient;

    @Parameter(property = "generateInterfaces", defaultValue = "false")
    private boolean generateInterfaces;

    @Parameter(property = "outputDir", defaultValue = "${project.build.directory}/generated-sources")
    private File outputDir;

    @Parameter(property = "exampleOutputDir", defaultValue = "${project.build.directory}/generated-examples")
    private File exampleOutputDir;

    @Parameter(property = "includeQueries")
    private String[] includeQueries;

    @Parameter(property = "includeMutations")
    private String[] includeMutations;

    @Parameter(property = "skipEntityQueries", defaultValue = "false")
    private boolean skipEntityQueries;

    @Parameter(property = "shortProjectionNames", defaultValue = "false")
    private boolean shortProjectionNames;

    @Parameter(property = "generateDataTypes", defaultValue = "true")
    private boolean generateDataTypes;

    @Parameter(property = "maxProjectionDepth", defaultValue = "10")
    private int maxProjectionDepth;

    @Parameter(property = "language", defaultValue = "java")
    private String language;

    @Parameter(property = "omitNullInputFields", defaultValue = "false")
    private boolean omitNullInputFields;

    @Parameter(property = "kotlinAllFieldsOptional", defaultValue = "false")
    private boolean kotlinAllFieldsOptional;

    @Parameter(property = "snakeCaseConstantNames", defaultValue = "false")
    private boolean snakeCaseConstantNames;

    private void verifySettings() {
        if (Objects.isNull(packageName)) {
            throw new RuntimeException("Please specify a packageName");
        }

        if (schemaPaths.length != Arrays.stream(schemaPaths).collect(Collectors.toSet()).size()) {
            throw new RuntimeException("Duplicate entries in schemaPaths");
        }

        for (final File schemaPath : schemaPaths) {
            if (!schemaPath.exists()) {
                try {
                    throw new RuntimeException("Schema File: " + schemaPath.getCanonicalPath() + " does not exist!");
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        verifySettings();

        final CodeGenConfig config = new CodeGenConfig(
                Collections.emptySet(),
                Arrays.stream(schemaPaths).collect(Collectors.toSet()), 
                outputDir.toPath(), 
                exampleOutputDir.toPath(),
                true, 
                packageName, 
                subPackageNameClient, 
                subPackageNameDatafetchers, 
                subPackageNameTypes,
                Language.valueOf(language.toUpperCase()), 
                generateBoxedTypes, 
                generateClient, 
                generateInterfaces,
                typeMapping, 
                Arrays.stream(includeQueries).collect(Collectors.toSet()),
                Arrays.stream(includeMutations).collect(Collectors.toSet()), 
                skipEntityQueries, 
                shortProjectionNames,
                generateDataTypes, 
                omitNullInputFields, 
                maxProjectionDepth, 
                kotlinAllFieldsOptional,
                snakeCaseConstantNames);

        getLog().info(String.format("Codegen config: %n%s", config));

        final CodeGen codeGen = new CodeGen(config);
        codeGen.generate();
    }
}
