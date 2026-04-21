package com.secretlabs.versioned_kv_store.service;

import com.secretlabs.versioned_kv_store.repository.RecordRepository;
import com.secretlabs.versioned_kv_store.repository.RecordVersionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@SpringBootTest
@Testcontainers
@DisplayName("Concurrency flood test")
class KvStoreFloodTest {
    @Autowired
    private KvStoreService kvStoreService;

    @Autowired
    private RecordRepository recordRepository;

    @Autowired
    private RecordVersionRepository recordVersionRepository;


    @BeforeEach
    void clean() {
        recordVersionRepository.deleteAll();
        recordRepository.deleteAll();
    }

    @Test
    @DisplayName("50 concurrent writes produce versions 1 through 51 with no gaps")
    void floodWith50ConcurrentRequests() throws InterruptedException {
        int threadCount = 50;
        String key = "flood-key";


        kvStoreService.upsert(key, "{\"initial\":\"true\"}");


        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startGate = new CountDownLatch(1);   // holds all threads
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        AtomicInteger errorCount = new AtomicInteger(0);
        AtomicInteger successCount = new AtomicInteger(0);


        IntStream.rangeClosed(1, threadCount).forEach(i ->
                executor.submit(() -> {
                    try {
                        startGate.await();  // all threads wait here
                        kvStoreService.upsert(key, "{\"request\":" + i + "}");
                        successCount.incrementAndGet();
                    } catch (Exception e) {
                        errorCount.incrementAndGet();
                        System.err.println("Thread " + i + " failed: " + e.getMessage());
                    } finally {
                        doneLatch.countDown();
                    }
                })
        );


        startGate.countDown();

        boolean completed = doneLatch.await(30, TimeUnit.SECONDS);

        executor.shutdown();


        assertThat(completed)
                .as("All 50 threads should complete within 30 seconds")
                .isTrue();


        assertThat(errorCount.get())
                .as("No thread should throw an exception")
                .isZero();


        assertThat(successCount.get())
                .as("All 50 writes should succeed")
                .isEqualTo(threadCount);


        Long recordId = recordRepository
                .findByKeyName(key)
                .orElseThrow()
                .getId();

        List<Integer> versions = recordVersionRepository
                .findAll()
                .stream()
                .filter(v -> v.getRecordEntity().getId().equals(recordId))
                .map(v -> v.getVersion())
                .sorted()
                .toList();


        assertThat(versions)
                .as("Should have exactly 51 versions")
                .hasSize(51);


        for (int i = 0; i < 51; i++) {
            assertThat(versions.get(i))
                    .as("Version at position %d should be %d", i, i + 1)
                    .isEqualTo(i + 1);
        }

        System.out.println("=== FLOOD TEST PASSED ===");
        System.out.println("Total versions: " + versions.size());
        System.out.println("Versions: " + versions);
    }
}
