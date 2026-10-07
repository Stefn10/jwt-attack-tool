# BVS Hybrid - Docker setup

One stack that runs all three components:

```
docker compose up
├── postgres   (PostgreSQL 17, database rafnovosti, seeded from db-init/)
└── tomcat     (Tomcat 9 + Java 8, with rva.war and jwt-tool.war)
```

## Quick

```bash
make demo
```

What it does:
1. `./build-wars.sh` - runs `mvn package` in `../app` and `../attack`, copies the WARs into `wars/`
2. `docker compose build` - builds a Tomcat image with both WARs
3. `docker compose up -d` - starts postgres + tomcat

Then open: **http://localhost:8080/jwt-tool/**

## Useful commands

| Command | What it does |
|---|---|
| `make demo` | everything at once (build + up) |
| `make up` | start only (if the WARs are already built) |
| `make logs` | follow the Tomcat logs |
| `make down` | stop the containers |
| `make clean` | down + delete volumes + delete the WARs |

## What lives where

```
docker/
├── docker-compose.yml      <- 2 services (postgres, tomcat) + bridge network
├── Dockerfile              <- Tomcat 9 with rva.war + jwt-tool.war
├── build-wars.sh           <- mvn package for both modules
├── Makefile                <- shortcuts
├── db-init/                <- schema.sql + seed.sql + creator user
└── wars/                   <- build artifacts (gitignored)
```

## DB host config

`app/.../repositories/PostgresAbstractRepository.java` reads `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` from environment variables. If they are missing it falls back to `localhost:5432/rafnovosti` (local dev mode). In docker compose the env vars point to the `postgres` service.

The JWT vulnerabilities are **not** touched - `UserService.java` stays frozen per the BVS strategy.

## Demo flow

```bash
git clone <repo>
cd jwt-attack-tool/docker
make demo
# wait ~20 seconds
open http://localhost:8080/jwt-tool/
```

Log in as `creator@raf.rs` / `admin`, click **RUN ALL** - all 10 attacks run one by one, animated.
