# Computer Science Foundations — Spring lab

A URL shortener that follows the System Design example in Computer Science Foundations: create a code (consistent), redirect from cache (available), count opens for the owner.

This repo is the runnable implementation. The study app keeps the map; you run these services in your own runtime.

## Requirements

Java 21. The Gradle wrapper (9.9.1) downloads a JDK 21 toolchain if your `JAVA_HOME` is newer.

The functional story is the URL Shortener example:

- A person submits a long URL and receives a short code.
- Opening the short code redirects to that long URL.
- The creator can read how many times the code was opened.

Public internet, browser client. Create is a store. Redirect and the count are retrieves (the click increment is a side write on redirect).

## CAP

- **Create** waits for a unique row in Postgres. Two people do not get the same code. If the insert collides, the service retries a new code.
- **Redirect** may answer from Redis. The mapping does not change, so a slightly old cache entry is still the right URL.
- **Count** is the number this service has recorded. It can lag a click that has not been written yet.

## Gateway

Spring Cloud Gateway on port 8080. It routes `/auth/**`, `/api/**`, and `GET /{code}` to the shortener. Rate limiting is a capability of this hop, not a separate box.

### Rate limit

A Redis token bucket shared by every gateway process (`capacity` 20, `refill-per-second` 10). An in-process count would multiply by the number of copies. Over the limit returns 429.

## Authentication

`POST /auth/register` and `POST /auth/login` issue a JWT. Create and stats require `Authorization: Bearer`. Redirect is public. The gateway checks the token on `/api/**` before the shortener sees the call.

## Database

One Postgres, Flyway from `shortener/src/main/resources/db/migration`. Shared schema. Versioned SQL is how the table changes.

## Cache

Redis cache-aside on redirect: `@Cacheable("links")` around the code → long URL lookup. Writes go to Postgres first.

## Observability

`X-Trace-Id` is set at the gateway and echoed by the shortener. Logs include `trace=`. Actuator exposes `/actuator/health` and `/actuator/metrics`. Tokens stay out of logs.

## Testing

| Strip | Where |
| --- | --- |
| Unit | `CodeGeneratorTest`, `AuthServiceTest` |
| Contract | `LinkControllerContractTest` (MockMvc + JWT) |
| Integration | `LinkFlowIT`, `TokenBucketRateLimiterTest` (Testcontainers Postgres/Redis) |
| End-to-end / smoke | `scripts/smoke.sh` against the gateway |

Canary, failover, partition, and duplicates wait until the diagram has more than one server or a queue.

## CI/CD

`.github/workflows/ci.yml` runs `./gradlew test` on pull requests and `main`. That is unit, contract, and Testcontainers integration. Smoke is local after `bootRun`.

## Run

```bash
docker compose up -d
./gradlew :shortener:bootRun
./gradlew :gateway:bootRun
./scripts/smoke.sh
```

Gateway: http://localhost:8080  
Shortener: http://localhost:8081

## Layout

```
commons/     JWT and trace id
gateway/     routing, JWT check, Redis token bucket
shortener/   auth, create, redirect, stats, Flyway, cache
```
