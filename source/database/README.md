# Database setup

MediSphere AI uses PostgreSQL 16. The schema itself is not hand-written SQL —
Hibernate creates and updates it from the JPA entities in
`backend/src/main/java/com/medisphere/**` (`spring.jpa.hibernate.ddl-auto: update`).

See [`../docs/database.md`](../docs/database.md) for the table-by-table
reference and design notes.

## Local setup

```bash
docker compose up -d
```

starts Postgres on `localhost:5432` with the credentials from `.env` (copy
`.env.example` first). No manual schema step is needed — starting the backend
creates every table, and `DataSeeder` populates realistic demo data on first boot.

## Production note

For a real deployment, replace `ddl-auto: update` with a versioned migration
tool (Flyway or Liquibase) so schema changes are reviewable and reversible —
`update` is convenient for a project at this stage but is not a production
migration strategy.
