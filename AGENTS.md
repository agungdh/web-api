# AGENTS.md

## Build & Run

```bash
./mvnw quarkus:dev           # dev mode, hot-reload, Dev UI at http://localhost:8080/q/dev/, Swagger UI at /q/swagger-ui/
./mvnw test                  # unit tests only
./mvnw verify                # unit tests only (skipITs=true by default)
./mvnw verify -DskipITs=false  # unit + integration tests
./mvnw package               # produce target/quarkus-app/quarkus-run.jar
./mvnw package -Dquarkus.package.jar.type=uber-jar  # single uber-jar
./mvnw package -Dnative      # native image via Mandrel (see Native Image below)
```

## Testing

- **Unit tests** use `@QuarkusTest` + REST Assured (`src/test/java/.../GreetingResourceTest.java`).
- **Integration tests** extend the unit test class and use `@QuarkusIntegrationTest` (`src/test/java/.../GreetingResourceIT.java`).
- **`skipITs` is `true` by default** — neither `verify` nor `test` runs integration tests. Pass `-DskipITs=false` or `-Dnative` (native profile sets `skipITs=false`).
- Run a single test class: `./mvnw test -Dtest=GreetingResourceTest`

## Java & Project Details

- **Java**: release 25, **Mandrel** distribution (GraalVM CE for native image). CI downloads Mandrel 25.0.3.0-Final via `jdkfile`.
- **Quarkus**: 3.36.0
- **Config format**: YAML (`src/main/resources/application.yml`). `quarkus-config-yaml` is a dependency — do NOT create `application.properties`.
- **Package**: `id.my.agungdh` — flat single-package layout, no sub-packages.
- **Maven wrapper**: `mvnw` uses Maven 3.9.15.

## Native Image

```bash
./mvnw package -Dnative -DskipTests
```

- **Mandrel required** — `setup-java` with `distribution: temurin` will NOT work for native builds. Use Mandrel.
- **`application.yml`** sets `quarkus.native.additional-build-args: -march=x86-64-v2` (target ISA level).
- Runtime GC for the native binary defaults to Serial. Pass `--gc=G1` in `additional-build-args` to use G1.
- Ubuntu native deps: `g++ zlib1g-dev libfreetype6-dev`
- **Optimization levels**: `0` (fastest build, debug), `1`, `2` (default, peak perf), `3` (experimental). Append `,-H:Optimize=0` to args for faster dev builds.

## Entity & Database Conventions

- **Base class**: All entities MUST extend `BaseEntity` (which extends `PanacheEntity`), never `PanacheEntity` directly.
- **Dual ID**: Internal `id` (Long, auto-increment, never exposed) + public `uuid` (UUID v4 via `UUID.randomUUID()` in `@PrePersist`). All REST endpoints reference the `uuid`.
- **Audit columns**: Every table has `created_by`, `created_at`, `updated_by`, `updated_at`, `deleted_by`, `deleted_at`. Timestamps auto-set via `@PrePersist` / `@PreUpdate` in `BaseEntity`.
- **Soft delete**: `@SQLRestriction("deleted_at IS NULL")` on `BaseEntity`. Use `softDelete(Long deletedBy)` — never `persist(delete)` or repository delete.
- **No `import.sql`**: All DDL/seed via Flyway migrations (`src/main/resources/db/migration/`). Do NOT create `import.sql`.
- **PostgreSQL only**: Use native queries when needed. Leverage partial indexes on `WHERE deleted_at IS NULL`, native UUID type, etc.

## CRUD & API Conventions

- **Pagination**: All GET list endpoints MUST be paginated. Use `PanacheQuery.page(Page.of(page, size))` + `PanacheQuery.count()` (count before pagination). Wrap the response in a `PagedResponse<T>` record:

  ```java
  public record PagedResponse<T>(List<T> data, long total, int page, int size) {}
  ```

  Resource method example:

  ```java
  @GET
  public PagedResponse<MyEntityDTO> getAll(@QueryParam("page") @DefaultValue("0") int page,
                                           @QueryParam("size") @DefaultValue("20") int size) {
      var query = MyEntity.findAll();
      var list = query.page(Page.of(page, size)).list();
      return new PagedResponse<>(MyEntityMapper.INSTANCE.toDTOs(list), query.count(), page, size);
  }
  ```

- **DTOs**: Use Java `record` for all request and response DTOs. Expose `uuid` — never expose the internal `id`. Naming convention: `<EntityName>DTO` (e.g., `MyEntityDTO`).

  ```java
  public record MyEntityDTO(UUID uuid, String field) {}
  ```

- **MapStruct**: Entity ↔ DTO conversion MUST use MapStruct (`quarkus-mapstruct`). Create one `@Mapper(componentModel = "cdi")` interface per entity and inject it into the resource class. Define both single-entity and list conversion methods.

  ```java
  @Mapper(componentModel = "cdi")
  public interface MyEntityMapper {
      MyEntityDTO toDTO(MyEntity entity);
      List<MyEntityDTO> toDTOs(List<MyEntity> entities);
      MyEntity toEntity(MyEntityDTO dto);
  }
  ```

## Infrastructure

```bash
docker compose up -d   # PostgreSQL, Valkey, MinIO, Adminer
```

| Service | Port | Credentials |
|---|---|---|
| PostgreSQL | 5432 | admin/admin, db: webapi |
| Valkey | 6379 | — |
| MinIO | 9000 (API), 9001 (console) | admin/adminadmin |
| Adminer | 8083 | — |

All ports bound to `127.0.0.1` only.

Flyway is configured (`quarkus.flyway.migrate-at-start: true`, `locations: classpath:db/migration`) but **no migrations exist yet** (only `.gitkeep`).

**No datasource is configured** in `application.yml` — add `quarkus.datasource.*` properties to wire up PostgreSQL.

## Docker

All Dockerfiles in `src/main/docker/`:

| Dockerfile | Base image | Use |
|---|---|---|
| `Dockerfile.jvm` | UBI 9 | JVM mode |
| `Dockerfile.legacy-jar` | UBI 9 | Legacy JAR |
| `Dockerfile.native` | UBI 9 minimal | Native |
| `Dockerfile.native-micro` | Quarkus micro | Native (smaller) |
| `Dockerfile.native-distroless` | `gcr.io/distroless/cc-debian12:nonroot` | **Production** (used in CI) |

**Run `./mvnw package` (or `-Dnative`) before building any Docker image** — they copy from `target/`.

## CI

GitHub Actions (`.github/workflows/ci.yml`) on push/PR to `main`:

1. Download Mandrel 25 tar.gz
2. Install via `setup-java@v4` with `distribution: jdkfile`
3. Install native deps (`g++`, `zlib1g-dev`, `libfreetype6-dev`)
4. `./mvnw verify -B` (unit tests only)
5. `./mvnw package -Dnative -DskipTests` (native image)
6. Build distroless Docker image, push to **GHCR** and **Docker Hub** (push only on actual `push` event, not PR)

Tags pushed: `latest`, `${{ github.sha }}` to both registries.

Required repo secrets: `DOCKERHUB_USERNAME`, `DOCKERHUB_TOKEN` (for Docker Hub push). GHCR uses `secrets.GITHUB_TOKEN`.

## Dependencies Not Yet Used

These are in `pom.xml` but no source file references them yet:

| Dependency | Purpose |
|---|---|
| `quarkus-mapstruct` | Object mapping / DTO conversion |
| `quarkus-hibernate-validator` | Bean validation |
| `quarkus-poi` | Excel generation |
| `quarkus-openpdf` | PDF generation |
| `lombok` | Boilerplate reduction |
