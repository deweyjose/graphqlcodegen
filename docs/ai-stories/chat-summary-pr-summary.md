    # PR Summary: May 2024

## Overview
This document summarizes the key changes and improvements made to the `graphqlcodegen` repository over the past couple of weeks, based on recent pull requests.

---

## Notable Pull Requests

### Dependency Updates
- **Kotlin**: Upgraded `org.jetbrains.kotlin:kotlin-compiler-embeddable` and `org.jetbrains.kotlin:kotlin-reflect` from 1.9.23 to 2.1.21, bringing in the latest bug fixes, performance improvements, and compatibility with new Gradle versions.
- **JaCoCo**: Upgraded `org.jacoco:jacoco-maven-plugin` from 0.8.12 to 0.8.13, adding support for Java 23/24, experimental Java 25 support, and improved Kotlin coverage reporting.
- **Build Helper**: Upgraded `org.codehaus.mojo:build-helper-maven-plugin` from 3.5.0 to 3.6.0, introducing parallel execution and deprecating some older goals.
- **Flatten Maven Plugin**: Upgraded `org.codehaus.mojo:flatten-maven-plugin` from 1.5.0 to 1.7.0, with new features, bug fixes, and dependency bumps.

### Features & Fixes
- **Type Mapping Improvements**:
  - PR #189: Now able to load properties files from `src/main/resources`, making type mapping configuration more flexible.
  - PR #183: Automatic scanning for `*-typemapping.properties` files on the classpath, reducing manual configuration and improving developer experience.

---

## Interaction Style in This Chat
In this session, the user requested a high-level summary of recent PRs, with a focus on actionable improvements and dependency management. The interaction was direct and goal-oriented, with a preference for concise, structured documentation. The user values automation, up-to-date dependencies, and clear communication of technical changes.

---

## Conclusion
The past couple of weeks have seen a strong focus on keeping dependencies current, improving type mapping flexibility, and maintaining compatibility with the latest Java and Kotlin releases. These changes help ensure the long-term maintainability and reliability of the `graphqlcodegen` project. 