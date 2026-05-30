# AGENTS.md

## Build & Run

```bash
./mvnw quarkus:dev           # dev mode, hot-reload, Dev UI at http://localhost:8080/q/dev/
./mvnw test                  # unit tests only
./mvnw verify                # unit + integration tests (but see note below)
./mvnw package               # produce target/quarkus-app/quarkus-run.jar
./mvnw package -Dquarkus.package.jar.type=uber-jar  # single uber-jar
./mvnw package -Dnative      # native image (requires GraalVM or -Dquarkus.native.container-build=true)
```

## Testing

- **Unit tests** use `@QuarkusTest` + REST Assured (`src/test/java/.../GreetingResourceTest.java`).
- **Integration tests** extend the unit test class and use `@QuarkusIntegrationTest` (`src/test/java/.../GreetingResourceIT.java`). They run the same assertions against the packaged artifact.
- **`skipITs` is `true` by default** in `pom.xml`. This means `./mvnw verify` will NOT run integration tests. To enable ITs, pass `-DskipITs=false` or activate the `native` profile (`-Dnative`).
- Run a single test class: `./mvnw test -Dtest=GreetingResourceTest`

## Key Project Details

- **Java**: release 25, Temurin distribution
- **Quarkus**: 3.36.0
- **PostgreSQL**: `quarkus-jdbc-postgresql` is a dependency but `application.properties` is empty — no datasource is configured. The `import.sql` file exists but all insert statements are commented out.
- **Package type**: `quarkus` (not `jar`). The quarkus-maven-plugin handles packaging.
- **Maven wrapper**: `mvnw` uses Maven 3.9.15.

## Docker

All Dockerfiles live in `src/main/docker/`. You MUST run `./mvnw package` (or the appropriate variant) before building any Docker image, since Dockerfiles copy from `target/`.

## CI

GitHub Actions (`ci.yml`) runs `./mvnw verify -B` on pushes/PRs to `main` with JDK 25 (Temurin).
