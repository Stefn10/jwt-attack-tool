# JWT Attack Tool + RAF Novosti

**BVS 2025/26 - Topic 4 (JWT security)**

---

## What this project is

This is a project for the Web Service Security course. It has two parts that work together:

1. **RAF Novosti** is a real REST application (news, comments, categories) with a full JWT login flow, but **intentionally vulnerable**. It carries the typical JWT mistakes seen in production.
2. **JWT Attack Tool** is a tool that **automatically exploits** those vulnerabilities, step by step, and shows what happens through an animated log.

### Mission

To show, in a controlled environment and against our **own** application, the most common ways a JWT authentication can be broken and, more importantly, **where exactly in the code** each vulnerability lives and how it is defended. For every attack there is a concrete line in the target application that explains why the attack succeeds (or why, on purpose, it fails).

> The tool is used **only** against our own test application, as part of a university project.

---

## Structure

```
jwt-attack-tool/
├── app/        <- RAF Novosti, the vulnerable JAX-RS application (attack target)
├── attack/     <- JWT tool, Jersey backend + HTML/JS frontend
└── docker/     <- docker-compose demo (Postgres + Tomcat + both WARs)
```

| Module | What it is | Tech |
|--------|-----------|------|
| `app/` | Vulnerable target | JAX-RS REST API, JWT auth |
| `attack/` | Exploitation tool | Java 8, Jersey, HTML/JS frontend |
| `docker/` | Demo environment | docker-compose, Postgres 17, Tomcat 9 |

---

## Quick start

### Prerequisites

The machine needs:

- **Docker** (with `docker compose`)
- **Make**
- **Maven** and a **JDK** (the build script packages both WARs locally before Docker picks them up)

### Step by step

The whole demo comes up with one command (`make demo`).

**1. Go into the `docker/` folder.** Every command below runs only from here, because `Makefile` and `build-wars.sh` look for `../app` and `../attack` relative to this location. If you are anywhere else, it will not work.

In the terminal:

```bash
cd jwt-attack-tool/docker
```

**2. Run the demo.**

In the terminal:

```bash
make demo
```

This single command runs three steps, in this order:

1. **Build the WARs** (`build-wars.sh`): Maven packages `app/` into `rva.war` and `attack/` into `jwt-tool.war`.
2. **Build the Docker image**: a Tomcat image with both WARs.
3. **Start the containers**: Postgres (the DB initializes itself from `db-init/`) and Tomcat.

**3. Wait for Tomcat to start.** The first run takes longer because Docker pulls images. To see when it is ready, follow the log:

In the terminal:

```bash
make logs
```

When the log shows Tomcat started and deployed both WARs, the demo is ready. Leave the log with `Ctrl+C` (the containers keep running in the background).

**4. Open the tool in the browser:**

http://localhost:8080/jwt-tool/

**5. Log in** as `creator@raf.rs` with the password `admin`.

**6. Click `RUN ALL`.** The tool runs all 10 attacks against RAF Novosti, one by one, animated. You can also run each attack on its own.

### Stopping and cleanup

Still from the `docker/` folder:

In the terminal:

```bash
make down     # stop containers (DB is kept)
make clean    # stop containers + delete the DB volume (clean start)
```

---

## Attacks

The tool exposes 10 attacks as REST endpoints (`POST /api/attack/{1..10}`); the frontend shows them step by step.

Note: some attacks **fail on purpose**. That is not a bug, it is proof that a defense works (for example signature verification or algorithm pinning).

| # | Attack | What it proves |
|---|--------|----------------|
| 1 | **Token Decoder** | The JWT payload is not encrypted, only Base64URL-encoded. Anyone who intercepts the token reads every claim (`role`, `userId`, email) in plain sight. |
| 2 | **Token Forgery** | Changes `role` to ADMIN but keeps the old signature. The server recomputes the HMAC, finds the mismatch and returns **401**. The attack fails on purpose, which proves signature verification works. |
| 3 | **Brute Force Secret** | Offline dictionary attack on the HMAC secret of a captured token. Since the target uses a weak secret (`"secret"`), the key falls in milliseconds. |
| 4 | **alg:none Bypass** | Classic CVE-2015-9235: a token with `alg:none` and no signature (4 case variants for case-insensitive bugs). The `java-jwt` library rejects it and returns **401**. |
| 5 | **Expired Replay** | Login never sets an `exp` claim, so the token is valid **forever**. Demo: a token with no `exp` passes, a forged `exp=2099` passes, but an `exp` in the past is rejected. |
| 6 | **Privilege Escalation** | Full-chain attack: chains A1 to A5 with the same secret, forges an ADMIN token and creates a new rogue admin account (`POST /users`). The whole takeover rests on the weak secret from A3. |
| 7 | **Algorithm Confusion** | CVE-2016-10555: the attacker uses the public RSA key as the HMAC secret and signs the token with `alg:HS256`. Everything is blocked, which means the target pins the algorithm. |
| 8 | **kid Header Injection** | Injects SQL / path-traversal / command payloads into the `kid` header (for example `../../etc/passwd`, `' UNION SELECT ...`). Watches the status and body for leakage (stack trace, SQL errors). |
| 9 | **No-Revocation Test** | A stateless JWT with no revocation list and no `exp` cannot be invalidated remotely. The logout endpoint does not exist (404), and the old token still works even after a password change. |
| 10 | **Token Surface Audit** | Passive audit of the transport surface: CORS (`*` + credentials), missing HSTS / CSP / X-Frame-Options, Set-Cookie flags (HttpOnly / Secure / SameSite). Crypto is only half the story. |

---

## Why `app/` is vulnerable

The vulnerabilities live in `app/src/.../services/UserService.java` and are left there on purpose for the demo:

- **Weak secret**: the HMAC signature uses `"secret"` (falls to a dictionary, see A3).
- **No `exp` claim**: the token is immortal (see A5, A10).
- **No `iss` / `iat`**: no extra validation of the token origin.
- **Role taken from the token without a DB re-check**: whoever forges the token gets the privileges (see A6).

---

## Building individual modules

```bash
cd app    && mvn package    # WAR of the vulnerable app
cd attack && mvn package    # WAR of the tool
```

`docker/` packages and deploys both on Tomcat; the DB initializes from `db-init/` (schema + seed + demo creator account).

---

## Defense

A detailed Q&A defense prep lives in `BVS_HIBRID_Projekat/BVS_DEFENSE_QA.md` (outside this repo, personal notes).

The key advantage at the defense: **both sides of the code are available**, so for every attack you can point to the exact line in `UserService.java` that causes it, and how the fix would look.

---

## Notes

- `app/` is a snapshot from **2026-05-20**. Active development of the RAF Novosti app (for the RWP course) lives in a separate repo.
- The JWT vulnerabilities are intentional and exist **only** for the BVS demonstration.
