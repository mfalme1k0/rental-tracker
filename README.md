# Rental Tracker

A CLI app for renting out your own items (a ladder, a drill...) and tracking who has what. Data lives in SQLite and
survives between runs. Java 21, Maven, layered architecture (transport / service / repository / domain / infrastructure).

**Start here:** [docs/architecture.md](docs/architecture.md) (layers, state machine, decisions and their reasons),
then [CONTRIBUTING.md](CONTRIBUTING.md) (branches, commits, PRs).

## Requirements
- JDK 21+, Maven 3.9+

## Build, test, run
```bash
mvn test                      # tests + coverage report (target/site/jacoco/index.html)
mvn verify -Pcoverage-gate    # same, and fails below 100% line/branch coverage
mvn compile exec:java         # run the CLI
```

## Status
Phase 0 (foundation) done: domain, state machine, exceptions, repository/service contracts, architecture tests.
Repositories, schema, services and menus are being built on top by the team.

## Team
Francis Kimani (lead), Fidel Shikokoti, Jackson Macharia
