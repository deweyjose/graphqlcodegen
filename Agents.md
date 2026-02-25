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

## Lessons learned (issue #302 session)

- A DGS config option can already exist in upstream `CodeGenConfig` and in our generated
  `GeneratedCodeGenConfigBuilder`, but still be **unusable** in Maven if it is not exposed in
  `Codegen.java` and wired through `CodegenExecutor`.
- Missing setter calls on the generated builder usually do **not** fail compilation because primitive
  fields default to `false`. This can hide feature gaps.
- Always verify upstream release notes before bumping versions. In this case, `graphql-dgs-codegen-core`
  was already on latest (`8.3.0`), so the right fix was wiring, not dependency upgrades.
- Avoid brittle test assertions on fixture counts. If adding a schema fixture, prefer checking for
  required fixture names rather than hardcoded totals.
- For this repo, run `spotless:apply` after edits; formatting changes are common in tests.

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

