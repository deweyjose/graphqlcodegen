# graphqlcodegen-example

A multi-module example project for the
[graphqlcodegen-maven-plugin](https://github.com/deweyjose/graphqlcodegen), vendored into the
plugin's repository and built as part of its reactor on every push.

It demonstrates the plugin's main features across four modules:

| Module | Demonstrates |
|--------|--------------|
| `common` | Schemas packaged in a jar (`META-INF/schema/`) + shared type-mapping properties |
| `server` | DGS Spring Boot server; local schema paths, remote `schemaUrls`, jar-embedded schemas, local + classpath type mappings |
| `client` | Typed client-API generation (`generateClientApi`, `includeQueries`) |
| `client-introspection` | Codegen from **live GraphQL introspection** (self-starts the server during the build) |

## Build

From the repository root (this installs the plugin first, then builds the examples against it):

```console
./mvnw -B -ntp install
```

See [AGENTS.md](AGENTS.md) for details, including how to build offline by overriding
`codegen.server.schemaUrl`.

## Run the demo

Start the server (GraphQL endpoint on `http://localhost:8080/graphql`):

```console
cd server
mvn spring-boot:run
```

Then run the client, which queries the server for shows and theaters using the generated typed
query API:

```console
cd ../client
mvn spring-boot:run
{shows=[{id=1, title=Stranger Things}, {id=2, title=Ozark}, {id=3, title=The Crown}, {id=4, title=Dead to Me}, {id=5, title=Orange is the New Black}]}
```
