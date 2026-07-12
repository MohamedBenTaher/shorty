package com.example.shorty.service;

import com.example.shorty.utils.Base64Encoder;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class IncrementServiceConcurrencyTest {

    @Test
    void parallelIdsShouldAllBeUnique() throws Exception {
        AtomicInteger counter = new AtomicInteger(0);
        int threadCount = 20;
        int idsPerThread = 500;
        Set<Integer> allIds = ConcurrentHashMap.newKeySet();
        List<Thread> threads = new ArrayList<>();

        for (int t = 0; t < threadCount; t++) {
            Thread thread = new Thread(() -> {
                for (int i = 0; i < idsPerThread; i++) {
                    int id = counter.incrementAndGet();
                    allIds.add(id);
                }
            });
            threads.add(thread);
            thread.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        assertThat(allIds).hasSize(threadCount * idsPerThread);
    }

    @Test
    void base62EncodingShouldProduceUniqueShortCodes() {
        int count = 10_000;
        Set<String> codes = new HashSet<>();
        for (int i = 1; i <= count; i++) {
            codes.add(Base64Encoder.encode(i));
        }
        assertThat(codes).hasSize(count);
    }

    @Test
    void base62EncodingShouldBeMonotonicallyGrowing() {
        String prev = Base64Encoder.encode(1L);
        for (int i = 2; i <= 10_000; i++) {
            String curr = Base64Encoder.encode((long) i);
            assertThat(curr).isNotEqualTo(prev);
            prev = curr;
        }
    }
}
