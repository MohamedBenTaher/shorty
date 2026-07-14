# Shorty — Distributed Snowflake

A URL shortener using **simplified Snowflake IDs** — each server generates globally unique IDs without any coordination.

## The Approach

Every short code comes from a 64-bit Snowflake ID assembled from three components:

```
┌──────────────────────┬────────────────┬───────────────────┐
│  Timestamp (42 bits) │ Shard (10 bits)│ Sequence (12 bits) │
│  ms since 2025-01-01 │  up to 1024    │  0–4095 per ms    │
└──────────────────────┴────────────────┴───────────────────┘
```

- **Timestamp:** Milliseconds since a custom epoch — ~139 years of headroom
- **Shard ID:** Auto-derived from hostname hash, or set via `app.shard-id` property
- **Sequence:** Per-millisecond counter, ~4 million IDs/sec per server

No database round-trip for ID generation. No central counter. No coordination between servers. Each instance operates independently, and the bit layout guarantees global uniqueness.

## Quick Start

```bash
docker compose up -d
./gradlew bootRun                     # shard auto-derived from hostname
./gradlew bootRun --args='--app.shard-id=42'  # explicit shard
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
| `id/SnowflakeIdGenerator.java` | Core algorithm — bit packing, sequence overflow, clock drift detection |
| `config/SnowflakeConfig.java` | Auto-derives shard ID from hostname MD5 hash |
| `service/ShorteningService.java` | Calls `idGenerator.nextId()` → Base62 → save |
| `model/UrlEntity.java` | Implements `Persistable<Long>` so explicit Snowflake IDs trigger INSERT |

## Run Tests

```bash
./gradlew test    # 44 tests — unit + integration + concurrency
```

**Key validations:**
- 100K sequential IDs → zero collisions
- 20 threads × 5K IDs on same shard → zero collisions
- Shard 0 and Shard 1 generating concurrently → no overlap
- Clock backward movement → `IllegalStateException` (fail fast)

## Explore Other Approaches

| Branch | Approach |
|---|---|
| `main` | Random Hash — stateless, unpredictable |
| `approach-2-unified-increment` | DB sequence — simple, compact |
| `approach-3-distributed-increment` | **You are here** |

Read the [case study](docs/case-study-url-shortener.md) for a deep dive comparing all three approaches.

## Tech Stack

Java 21 · Spring Boot 3.4 · Spring Data JDBC · PostgreSQL · Docker Compose · JUnit 5 · Mockito · Gradle
