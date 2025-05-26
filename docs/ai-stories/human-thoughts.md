# Automating GraphQL Codegen Maven Plugin Maintenance

## Project Background

I've managed an open-source project called [`deweyjose/graphqlcodegen`](https://github.com/deweyjose/graphqlcodegen), which wraps Netflix's DGS GraphQL codegen library in a Maven plugin. Netflix, being primarily a Gradle shop, opted not to support the Maven ecosystem, so I created this plugin to bridge that gap. The primary challenge has always been mapping Maven plugin parameters to Netflix's `CodeGenConfig` constructor and maintaining thorough documentation for these parameters.

## AI-Assisted Development Journey

### JetBrains AI Assistant (Early Experience)

Initially, I explored JetBrains' AI Assistant plugin to automate much-needed improvements. It excelled at ideation, quickly identifying parameter mismatches between my implementation and Netflix's. We settled on a strategy:

* Generate my plugin's main entry point (`@Mojo`) using a new Maven plugin: `graphqlcodegen-param-plugin`.
* Use reflection and deep introspection on the Netflix `CodeGenConfig` class to auto-generate Maven `@Parameter` annotations and documentation.
* Maintain backward compatibility by keeping old parameters but introducing new ones with corrected names.

Unfortunately, JetBrains' AI Assistant, while good at ideation, performed poorly in actual execution. It felt pre-alpha rather than beta, ultimately leading me to request a refund.

### Switching to Cursor

Cursor completely transformed my workflow and quickly proved its value:

* Within 30 minutes, Cursor helped me refactor the project into a multi-module Maven project, setting the stage for future automation.
* I utilized Cursor's GitHub MCP server to easily point the tool at authoritative sources for configuration (`CodeGenConfig`) rather than relying on reflection.
* Cursor demonstrated exceptional understanding of Maven, idiomatic Java, and test-driven workflows.
* Running JUnit tests directly within Cursor has dramatically reduced my dependence on IntelliJ.

### Automated Parameter Extraction and Code Generation

Key milestones in today's session included:

* **Automated Parameter Extraction**: Built a Maven plugin (`ParamCodegen`) to pull the latest `CodeGenConfig.kt` from GitHub, using Kotlin PSI (compiler-embedded) for robust parameter extraction.
* **Minimal Test Resource**: Created a streamlined `CodeGenConfig.kt` for reliable, fast testing.
* **Type Mapping Strategy**: Implemented a robust mapping of Kotlin types to idiomatic Java/Maven field types.
* **JavaPoet Integration**: Used JavaPoet to generate the new `AutoCodegen` class, complete with appropriate Maven annotations and parameter fields.
* **Build System Improvements**: Cleaned Maven dependency scopes and ensured compile-time availability of dependencies.
* **Comprehensive Test Automation**: Ensured end-to-end tests from Kotlin parsing to Java code generation.

### Increased Test Coverage

Test coverage dramatically improved:

* **Before**: 30–50% (limited unit and integration tests).
* **After**: 80–95%+, with robust end-to-end validation.
* Enhanced error handling and comprehensive coverage of edge cases.

### Practical Cursor Experiences

* Leveraged Cursor's conversational AI to seamlessly transcribe existing code blocks into JavaPoet-generated code.
* Effortlessly instructed Cursor to add getters to newly generated class fields.
* During late-night refactoring, Cursor automated tedious tasks—running `mvn clean install`, fixing compile errors, and adjusting constructor calls—allowing me to focus on higher-value tasks despite fatigue.

## Reflections

Cursor’s capabilities have dramatically reshaped how I maintain and evolve this Maven plugin, making development enjoyable and efficient. It reduced manual, repetitive tasks and boosted overall code quality and test coverage. This experience underscores the potential of well-integrated AI tooling in developer workflows.
