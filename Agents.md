# Agents.md

## Purpose

Quick guide for cloud/cursor agents working in this repository.

## Repository layout

- `graphqlcodegen-bootstrap-plugin`: helper plugin that generates `GeneratedCodeGenConfigBuilder` from upstream DGS `CodeGen.kt`.
- `graphqlcodegen-maven-plugin`: actual Maven plugin users consume.
- root `pom.xml`: parent/dependency management for both modules.

## Common workflow for DGS option updates

1. Check issue + upstream Netflix DGS release notes.
2. Confirm `graphql-dgs-codegen-core.version` in root `pom.xml`.
3. Expose new option in:
   - `graphqlcodegen-maven-plugin/src/main/java/.../Codegen.java` (`@Parameter`)
   - `CodegenConfigProvider.java` (getter method)
   - `CodegenExecutor.java` (forward to `GeneratedCodeGenConfigBuilder`)
4. Add/update tests in `CodegenExecutorTest` and fixtures under `src/test/resources/schema`.
5. Document option in `README.md`.

## Build/test commands

Prefer Maven wrapper:

```bash
./mvnw spotless:apply
./mvnw -N install
./mvnw -pl graphqlcodegen-bootstrap-plugin -DskipTests install
./mvnw -pl graphqlcodegen-maven-plugin test
```

## Important gotcha

If building/testing only `graphqlcodegen-maven-plugin`, Maven must resolve:

- parent POM (`graphqlcodegen-parent`) in local repo
- locally built `graphqlcodegen-bootstrap-plugin` (used during `generate-sources`)

If missing, run:

```bash
./mvnw -N install
./mvnw -pl graphqlcodegen-bootstrap-plugin -DskipTests install
```

## Release version bump

Current plugin artifact version is in:

- `graphqlcodegen-maven-plugin/pom.xml` -> `<version>`

After bumping:

1. run `./mvnw spotless:apply`
2. commit
3. push branch

