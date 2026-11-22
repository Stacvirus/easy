# ADR 0001: Use Liquibase migrations

Status: accepted. The implementation plan allows Flyway unless the reference project supports another justified convention. The inspected reference backend uses Liquibase with a master XML changelog and versioned SQL changesets. This project adopts the same approach to preserve familiar migration practice. Hibernate validates rather than creates schema. The tradeoff is retaining an XML index for SQL migrations.
