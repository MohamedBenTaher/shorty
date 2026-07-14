# Shorty — Unified Increment

A URL shortener using **PostgreSQL sequence-based ID generation** — the simplest way to guarantee unique, compact short codes.

## The Approach

Every short code is a Base62 encoding of the next value from a PostgreSQL sequence. The database owns the counter, making uniqueness guaranteed and atomic.

```
Long URL → Check dedup → SELECT nextval('urls_id_seq') → Base62 → short code
```

**Why it works:**
- `nextval()` is atomic — two concurrent requests never get the same number
- Starting from 1 means codes grow slowly: single-character for the first 62 URLs
- The implementation is ~10 lines of actual logic

## Quick Start

```bash
docker compose up -d
./gradlew bootRun
```

Open `http://localhost:8080`.

## API

```
POST /api/shorten          { "longUrl": "https://..." }
→   { "shortUrl": "...", "shortCode": "..." }

GET  /s/{shortCode}        → 302 redirect to original URL
```

## Key Files

| File | Role |
|---|---|
| `service/ShorteningService.java` | Calls `nextId()`, encodes to Base62, saves with explicit ID |
| `repository/UrlRepository.java` | `@Query("SELECT nextval('urls_id_seq')") Long nextId()` |
| `model/UrlEntity.java` | Implements `Persistable<Long>` so explicit IDs trigger INSERT |

## Run Tests

```bash
./gradlew test    # 38 tests — unit + integration + concurrency
```

**Concurrency test:** 20 threads × 500 IDs each → zero collisions. Validates that the sequence is truly atomic.

## Explore Other Approaches

| Branch | Approach |
|---|---|
| `main` | Random Hash — stateless, unpredictable |
| `approach-2-unified-increment` | **You are here** |
| `approach-3-distributed-increment` | Distributed Snowflake — zero coordination |

Read the [case study](docs/case-study-url-shortener.md) for a deep dive comparing all three approaches.

## Tech Stack

Java 21 · Spring Boot 3.4 · Spring Data JDBC · PostgreSQL · Docker Compose · JUnit 5 · Mockito · Gradle
