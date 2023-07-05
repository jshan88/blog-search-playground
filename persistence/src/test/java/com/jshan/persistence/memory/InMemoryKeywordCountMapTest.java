package com.jshan.persistence.memory;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class InMemoryKeywordCountMapTest {

    @Test
    @DisplayName("Concurrency_Atomicity보장_ConcurrentHashMap")
    void givenConcurrentHashMap_whenConcurrentMerge_thenStillAtomicityGuaranteed() throws InterruptedException {

        // GIVEN
        Map<String, Integer> keywordCounts = new ConcurrentHashMap<>();
        String keyword = "keyword";

        int threadCounts = 50;
        int executePerThread = 50;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCounts);
        CountDownLatch countDownLatch = new CountDownLatch(threadCounts * executePerThread);

        // threadCounts 만큼의 쓰레드가 각각 executePerThread 만큼 merge 수행
        for(int i = 0; i < threadCounts; i++) {
            executorService.execute(() -> {
                for(int j = 0; j < executePerThread; j++) {
                    keywordCounts.merge(keyword, 1, Integer::sum);
                    countDownLatch.countDown();
                }
            });
        }

        countDownLatch.await();

        // THEN
        assertEquals(0, countDownLatch.getCount());
        assertEquals(threadCounts * executePerThread, keywordCounts.get(keyword));
    }
}
