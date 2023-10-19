package com.jshan.persistence.memory;

import com.google.common.collect.MinMaxPriorityQueue;
import com.google.common.collect.Queues;
import com.jshan.persistence.KeywordCount;
import java.util.Collections;
import java.util.Comparator;
import java.util.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 최상위 검색 키워드를 위한 인메모리 데이터 구조 생성
 */
@Configuration
public class InMemoryTopKeywordsQueue {

    /**
     * 최상위 검색 키워드를 저장하기 위한 동기화 큐를 생성 <br>
     * 큐는 키워드 개수를 기준으로 내림차순으로 정렬. 큐의 최대 크기는 10으로 설정 (요구사항) <br>
     * Guava 라이브러리 활용
     *
     * @return 최상위 검색 키워드를 나타내는 {@link KeywordCount} 객체를 담은 {@link MinMaxPriorityQueue}
     * @see Queues#synchronizedQueue(Queue)
     */
    @Bean
    public Queue<KeywordCount> topKeywords() {
        return Queues.synchronizedQueue(MinMaxPriorityQueue
            .orderedBy(Comparator.comparing(KeywordCount::getCount).reversed())
            .maximumSize(10)
            .create());
    }
}
