package com.example.shorty.id;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Simplified Snowflake-style distributed unique ID generator.
 *
 * Bit layout: [timestamp: 42 bits][shard: 10 bits][sequence: 12 bits] = 64 bits
 *
 * Timestamp: milliseconds since 2025-01-01 (custom epoch).
 *  42 bits → ~139 years of IDs before rollover.
 *
 * Shard: 10 bits → up to 1024 worker nodes without coordination.
 *
 * Sequence: 12 bits → 4096 IDs per millisecond per shard (~4M IDs/sec per worker).
 */
public class SnowflakeIdGenerator {

    private static final long CUSTOM_EPOCH = 1735689600000L;
    private static final int SHARD_BITS = 10;
    private static final int SEQUENCE_BITS = 12;
    private static final int SHARD_SHIFT = SEQUENCE_BITS;
    private static final int TIMESTAMP_SHIFT = SEQUENCE_BITS + SHARD_BITS;
    private static final long MAX_SEQUENCE = (1L << SEQUENCE_BITS) - 1;
    private static final long MAX_SHARD_ID = (1L << SHARD_BITS) - 1;

    private final long shardId;
    private final AtomicLong lastTimestamp = new AtomicLong(-1L);
    private final AtomicLong sequence = new AtomicLong(0L);

    public SnowflakeIdGenerator(long shardId) {
        if (shardId < 0 || shardId > MAX_SHARD_ID) {
            throw new IllegalArgumentException(
                "shardId must be between 0 and " + MAX_SHARD_ID + ", got: " + shardId);
        }
        this.shardId = shardId;
    }

    public synchronized long nextId() {
        long currentMillis = currentTimeMillis();
        long last = lastTimestamp.get();

        if (currentMillis < last) {
            throw new IllegalStateException(
                "Clock moved backwards! Refusing to generate ID. " +
                "Last: " + last + "ms, Current: " + currentMillis + "ms");
        }

        if (currentMillis == last) {
            long seq = sequence.incrementAndGet();
            if (seq > MAX_SEQUENCE) {
                while (currentMillis <= last) {
                    currentMillis = currentTimeMillis();
                }
                sequence.set(0L);
                seq = 0L;
            }
        } else {
            sequence.set(0L);
        }

        lastTimestamp.set(currentMillis);

        return ((currentMillis - CUSTOM_EPOCH) << TIMESTAMP_SHIFT)
             | (shardId << SHARD_SHIFT)
             | sequence.get();
    }

    public long getShardId() {
        return shardId;
    }

    long currentTimeMillis() {
        return System.currentTimeMillis();
    }
}
