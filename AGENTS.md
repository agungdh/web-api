# AGENTS.md

## Build & Run

```bash
./mvnw quarkus:dev           # dev mode, hot-reload, Dev UI at http://localhost:8080/q/dev/
./mvnw test                  # unit tests only
./mvnw verify                # unit tests only (skipITs=true by default — see Testing)
./mvnw verify -DskipITs=false  # unit + integration tests
./mvnw package               # produce target/quarkus-app/quarkus-run.jar
./mvnw package -Dquarkus.package.jar.type=uber-jar  # single uber-jar
./mvnw package -Dnative      # native image (requires GraalVM or -Dquarkus.native.container-build=true)
```

## Testing

- **Unit tests** use `@QuarkusTest` + REST Assured (`src/test/java/.../GreetingResourceTest.java`).
- **Integration tests** extend the unit test class and use `@QuarkusIntegrationTest` (`src/test/java/.../GreetingResourceIT.java`). They run the same assertions against the packaged artifact.
- **`skipITs` is `true` by default** in `pom.xml`. Both `./mvnw verify` and `./mvnw test` will NOT run integration tests. To enable ITs, pass `-DskipITs=false` or activate the `native` profile (`-Dnative`).
- Run a single test class: `./mvnw test -Dtest=GreetingResourceTest`

## Key Project Details

- **Java**: release 25, Temurin distribution
- **Quarkus**: 3.36.0
- **Config format**: YAML (`src/main/resources/application.yml`), not `application.properties`. Enabled by `quarkus-config-yaml` dependency.
- **Package**: `id.my.agungdh` — flat single-package layout, no sub-packages.
- **Entity pattern**: Panache **Active Record** — all entities extend `BaseEntity extends PanacheEntity`, not `PanacheEntity` directly.
- **Annotation processing**: Lombok is configured in `maven-compiler-plugin`. MapStruct (`quarkus-mapstruct`) is a dependency but not yet used in any source file.
- **Swagger UI** is always included (`quarkus.swagger-ui.always-include: true`).
- **Package type**: `quarkus` (not `jar`). The quarkus-maven-plugin handles packaging.
- **Maven wrapper**: `mvnw` uses Maven 3.9.15.

## Entity & Database Conventions

- **Base class**: All entities MUST extend `BaseEntity` (which extends `PanacheEntity`), never `PanacheEntity` directly.
- **Dual ID**: Internal `id` (Long, auto-increment, never exposed) + public `uuid` (UUID v4 via `UUID.randomUUID()` in `@PrePersist`). All REST endpoints reference the `uuid`, never the internal `id`.
- **Audit columns**: Every table has `created_by` (Long, FK to user.id, nullable), `created_at` (Instant/TIMESTAMPTZ, UTC), `updated_by` (Long, nullable), `updated_at` (Instant/TIMESTAMPTZ), `deleted_by` (Long, nullable), `deleted_at` (Instant/TIMESTAMPTZ). Timestamps auto-set via `@PrePersist` / `@PreUpdate` hooks in `BaseEntity`.
- **Soft delete**: All entities use soft delete — `deleted_at IS NULL` is enforced via `@SQLRestriction` on `BaseEntity`. Use `softDelete(Long deletedBy)` method instead of `persist(delete)` or repository delete.
- **PostgreSQL only**: Queries optimized for PostgreSQL. Use native queries when performance matters. Leverage PostgreSQL-specific features (partial indexes on `WHERE deleted_at IS NULL`, native UUID type, etc.).
- **No `import.sql`**: All DDL and seed data go through Flyway migrations (`src/main/resources/db/migration/`). `import.sql` is deleted and must never be re-created.

## Database, Migrations, & Infrastructure

- **PostgreSQL**: `quarkus-jdbc-postgresql` is a dependency but **no datasource is configured** in `application.yml`. Add `quarkus.datasource.*` properties to wire it up.
- **Flyway** is configured (`quarkus.flyway.migrate-at-start: true`) and expects migration scripts in `src/main/resources/db/migration/`. Currently only `.gitkeep` — no migrations exist yet.
- **docker-compose.yml** provides: PostgreSQL (port 5432, user/pass: admin/admin, db: webapi), Valkey (port 6379), MinIO (ports 9000/9001, user: admin, pass: adminadmin), and Adminer (port 8083). Start with `docker compose up -d`.

## Dependencies Available But Not Yet Used

| Dependency | Purpose |
|---|---|
| `quarkus-mapstruct` | Object mapping / DTO conversion |
| `quarkus-hibernate-validator` | Bean validation (`@NotNull`, `@Size`, etc.) |
| `quarkus-poi` | Excel file generation |
| `quarkus-openpdf` | PDF generation |
| `lombok` | Boilerplate reduction (configured as annotation processor, no classes use it yet) |

## Docker

All Dockerfiles live in `src/main/docker/`. You MUST run `./mvnw package` (or the appropriate variant) before building any Docker image, since Dockerfiles copy from `target/`.

## CI

GitHub Actions (`ci.yml`) runs `./mvnw verify -B` on pushes/PRs to `main` with JDK 25 (Temurin). Since `skipITs=true` in pom.xml, CI only runs unit tests.
