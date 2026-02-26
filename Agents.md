# Agents.md

## Purpose

Quick guide for cloud/cursor agents working in this repository.

## Repository layout

- `graphqlcodegen-maven-plugin`: actual Maven plugin users consume.
- `graphqlcodegen-maven-plugin/src/main/java/io/github/deweyjose/graphqlcodegen/CodeGenConfigBuilder.java`: checked-in builder that mirrors upstream DGS `CodeGenConfig`.
- root `pom.xml`: parent/dependency management.

## Common workflow for DGS option updates

1. Check issue + upstream Netflix DGS release notes.
2. Confirm `graphql-dgs-codegen-core.version` in root `pom.xml`.
3. Expose new option in:
   - `graphqlcodegen-maven-plugin/src/main/java/.../Codegen.java` (`@Parameter`)
   - `CodegenConfigProvider.java` (getter method)
   - `CodegenExecutor.java` (forward to `CodeGenConfigBuilder`)
4. Add/update tests in `CodegenExecutorTest` and fixtures under `src/test/resources/schema`.
5. Document option in `README.md`.

## Lessons learned (issue #302 session)

- A DGS config option can already exist in upstream `CodeGenConfig` and in our checked-in
  `CodeGenConfigBuilder`, but still be **unusable** in Maven if it is not exposed in
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
./mvnw -pl graphqlcodegen-maven-plugin test
```

## Important gotcha

This repo keeps `CodeGenConfigBuilder` checked in. When bumping
`graphql-dgs-codegen-core.version`, make sure builder constructor args and setter surface still match
upstream `CodeGenConfig`, then wire any new options through `Codegen.java`/`CodegenConfigProvider.java`/`CodegenExecutor.java`.

## Release version bump

Current plugin artifact version is in:

- `graphqlcodegen-maven-plugin/pom.xml` -> `<version>`

After bumping:

1. run `./mvnw spotless:apply`
2. commit
3. push branch

