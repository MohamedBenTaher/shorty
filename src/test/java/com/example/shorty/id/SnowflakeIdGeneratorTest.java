package com.example.shorty.id;

import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SnowflakeIdGeneratorTest {

    @Test
    void shouldGenerateUniqueIds() {
        SnowflakeIdGenerator generator = new SnowflakeIdGenerator(0);
        int count = 100_000;
        Set<Long> ids = ConcurrentHashMap.newKeySet();

        for (int i = 0; i < count; i++) {
            ids.add(generator.nextId());
        }

        assertThat(ids).hasSize(count);
    }

    @Test
    void shouldBeMonotonicWithinSameShard() {
        SnowflakeIdGenerator generator = new SnowflakeIdGenerator(42);
        long prev = generator.nextId();

        for (int i = 0; i < 10_000; i++) {
            long next = generator.nextId();
            assertThat(next).isGreaterThan(prev);
            prev = next;
        }
    }

    @Test
    void differentShardsShouldProduceDifferentRanges() {
        SnowflakeIdGenerator shard0 = new SnowflakeIdGenerator(0);
        SnowflakeIdGenerator shard1 = new SnowflakeIdGenerator(1);

        Set<Long> allIds = ConcurrentHashMap.newKeySet();
        for (int i = 0; i < 1000; i++) {
            allIds.add(shard0.nextId());
            allIds.add(shard1.nextId());
        }

        assertThat(allIds).hasSize(2000);
    }

    @Test
    void parallelGeneratorsOnSameShardShouldNotCollide() throws Exception {
        SnowflakeIdGenerator generator = new SnowflakeIdGenerator(5);
        int threadCount = 20;
        int idsPerThread = 5000;
        Set<Long> allIds = ConcurrentHashMap.newKeySet();
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int t = 0; t < threadCount; t++) {
            executor.submit(() -> {
                try {
                    for (int i = 0; i < idsPerThread; i++) {
                        allIds.add(generator.nextId());
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        assertThat(allIds).hasSize(threadCount * idsPerThread);
    }

    @Test
    void shouldRejectNegativeShardId() {
        assertThatThrownBy(() -> new SnowflakeIdGenerator(-1))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("shardId");
    }

    @Test
    void shouldRejectShardIdAbove1023() {
        assertThatThrownBy(() -> new SnowflakeIdGenerator(1024))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("shardId");
    }

    @Test
    void shouldAcceptMaxShardId() {
        SnowflakeIdGenerator generator = new SnowflakeIdGenerator(1023);
        assertThat(generator.getShardId()).isEqualTo(1023);
        assertThat(generator.nextId()).isPositive();
    }

    @Test
    void shouldThrowOnClockBackwardMove() {
        java.util.concurrent.atomic.AtomicLong fakeTime = new java.util.concurrent.atomic.AtomicLong(1_000_000L);

        SnowflakeIdGenerator generator = new SnowflakeIdGenerator(0) {
            @Override
            long currentTimeMillis() {
                return fakeTime.get();
            }
        };

        generator.nextId();

        fakeTime.set(500_000L);

        assertThatThrownBy(() -> generator.nextId())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Clock moved backwards");
    }

    @Test
    void shouldBeFasterThan1MsPerId() {
        SnowflakeIdGenerator generator = new SnowflakeIdGenerator(7);
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            generator.nextId();
        }
        long elapsed = System.currentTimeMillis() - start;
        assertThat(elapsed).isLessThan(200);
    }
}
