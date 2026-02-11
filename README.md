# Unified User Management System (UUMS)

Initial project scaffold for a multi-module user-management platform:

- `uums-api`: Spring Boot API (Security, OAuth2 resource server, Liquibase, JPA).
- `uums-batch`: Spring Batch job runner for analytics/reporting pipelines.
- `uums-ui`: React + Tailwind UI starter.
- `memory`: Daily development worklogs with chronological timestamped updates.
- `scripts`: Local lifecycle helpers.

## Quick start

```bash
./scripts/start.sh
```

Stop:

```bash
./scripts/stop.sh
```

Restart:

```bash
./scripts/restart.sh
```

## Build and test

```bash
mvn test
```

## Worklog policy

A new file is created per day under `memory/` with ISO date naming, and entries are appended with timestamps in chronological order.
