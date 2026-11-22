# Reference backend analysis

Inspected `/home/stac/dev/wassa/staff-folder/staff-folder-backend` on 21 November 2026. The reference was not modified. This analysis is based on its `pom.xml`, source tree, application configuration, representative controller, service, mapper, storage interface, security configuration, exception handler, migrations and test tree.

## Architecture and layout

The application is a **layered monolith**, primarily package by technical layer under `com.ccabank.staff_folder`. It has `controllers`, `dtos/requests`, `dtos/responses`, `services/interfaces`, `services/implementations`, `entities`, `repositories`, `mappers`, `exceptions`, `configurations`, `properties`, and `clients`. Some subpackages split infrastructure concerns such as `configurations/security` and `repositories/specifications`. The Spring entry point is `StaffFolderApplication`.

Controllers use request/response DTOs and delegate to services; they do not expose JPA entities. Services own business rules and transactions (`@Service`, `@Transactional`), with constructor injection via Lombok `@RequiredArgsConstructor`. Interfaces are used for services with a meaningful boundary, notably `StorageService`. Mappers turn entities into response DTOs. Spring Data JPA repositories own persistence access.

`GlobalExceptionHandler` maps business and framework exceptions to a consistent `ApiError` with stable codes. Request validation uses Jakarta Bean Validation. Configuration is in `application.yaml`, with typed `@ConfigurationProperties` records for grouped settings. Actuator supplies health and info endpoints. Security uses stateless Spring Security and Keycloak JWTs; controller access is based on the authenticated identity. `RequestLoggingFilter` validates or generates a correlation ID and places it in MDC; `AsyncConfig` propagates MDC to async tasks. The project uses Java 21, Maven, Spring Boot 4.1.1, PostgreSQL, Liquibase, and Springdoc OpenAPI. Liquibase has a master XML changelog that includes versioned SQL files. Tests include Mockito service tests and `MockMvc` integration tests against PostgreSQL, with S3 replaced by an in-memory test adapter.

## Conventions adopted

- Layered packages, thin controllers, request/response DTOs, service-owned transactions, explicit mappers and Spring Data repositories.
- Constructor injection; meaningful JavaDoc on contracts and important business rules.
- A consistent API error representation and central exception mapping once feature endpoints arrive.
- Typed configuration properties for storage, OCR and job settings when those integrations are introduced.
- PostgreSQL migrations through Liquibase, since this is the reference project's established convention. Use a master changelog and additive versioned SQL changesets.
- Actuator health endpoint and focused service and integration tests.

## Patterns not carried over

- Keycloak, Feign and OAuth resource-server wiring serve the reference's existing identity provider. This project has no such provider; a simple application-owned auth flow is planned for the MVP.
- Its bank-specific dossier model, legacy account linking, i18n content, document encryption scheme, and Excel export do not fit this domain.
- One interface per service is unnecessary. Keep interfaces for genuine boundaries, such as storage and document processing.
- The reference contains broad exception and DTO catalogs. Add only types needed for implemented features.
- Its `@EnableAsync` configuration propagates logging context but does not itself make work durable. Conversion jobs need persisted state and restart recovery.
- The reference configuration has credential defaults in source. Those values must never be copied; this project uses local-only defaults and requires environment supplied credentials for shared deployments.

## Proposed backend layout

```text
com.example.documentdigitization
├── controllers
├── dtos.requests / dtos.responses
├── services                 # add interfaces/implementations only where useful
├── entities
├── repositories
├── mappers
├── exceptions
├── configurations
└── properties
```

This follows the reference's technical layers. Feature boundaries are maintained in services and domain types without imposing a second package architecture.

## Decisions and open questions

The first increment uses Java 21, Maven and Liquibase. Authentication and exact OCR engine choice remain feature decisions; the latter depends on real handwriting benchmarks. The API-to-OCR contract will be specified before integration. The reference uses Spring Boot 4.1.1, so the bootstrap matches it; dependency compatibility will be verified by a build.
