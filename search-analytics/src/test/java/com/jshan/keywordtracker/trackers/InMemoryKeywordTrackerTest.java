package com.jshan.keywordtracker.trackers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.MinMaxPriorityQueue;
import com.google.common.collect.Queues;
import com.jshan.persistence.KeywordCount;
import com.jshan.persistence.database.repository.TopKeywordRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.bytebuddy.utility.RandomString;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

class InMemoryKeywordTrackerTest {

    @Mock
    private TopKeywordRepository topKeywordRepository;

    @Test
    @DisplayName("Concurrency_Atomicity보장_InMemoryKeywordTracer.onSearch()")
    void givenInMemoryPersistence_whenConcurrentSearchInvoked_thenStillConcurrencySafe() throws InterruptedException {
        // GIVEN
        Map<String, Integer> keywordCounts = new ConcurrentHashMap<>();
        Queue<KeywordCount> topKeywords = Queues.synchronizedQueue(MinMaxPriorityQueue
                                                                       .orderedBy(Comparator.comparing(KeywordCount::getCount).reversed())
                                                                       .maximumSize(10)
                                                                       .create());

        InMemoryKeywordTracker keywordTracker = new InMemoryKeywordTracker(keywordCounts, topKeywords, topKeywordRepository);

        // 쓰레드 및 쓰레드 별 작업 반복 횟수 세팅
        int threadCount = 10;
        int executePerThread = 1000;
        int searchCount = 100;

        // searchCount 횟수만큼 조회 시, 1자리 랜덤한 키 생성. randomKeyWords.size() = onSearch 작업 예정 횟수
        List<String> randomKeywords = new ArrayList<>();
        for(int i = 0; i < searchCount; i++) {
            randomKeywords.add(RandomString.make(1));
        }

        // threadCount 개의 쓰레드가 executePerThread 횟수 만큼 작업 수행
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch countDownLatch = new CountDownLatch(threadCount * executePerThread);

        for(int i = 0; i < threadCount * executePerThread; i++) {
            executorService.execute(() -> {
                randomKeywords.forEach(keywordTracker::onSearch);
                countDownLatch.countDown();
            });
        }

        countDownLatch.await();

        // Map 에 저장된 키워드들의 검색 카운트 총 합
        int totalCounts = keywordCounts.values().stream().mapToInt(i -> i).sum();
        // Map 에 저장된 키워드 중 가장 큰 검색 카운트
        int maximumValueInMap = keywordCounts.values().stream().max(Comparator.comparingInt(Integer::intValue)).get();

        // THEN
        // ConcurrentHashMap (keywordCounts) 의 원자성 체크
        assertEquals(threadCount * executePerThread * searchCount, totalCounts);

        // Synchronous MinMaxPriorityQueue (topKeywords) 체크
        assertEquals(maximumValueInMap, topKeywords.peek().getCount());
    }
}
