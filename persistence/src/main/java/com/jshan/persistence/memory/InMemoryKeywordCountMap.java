package com.jshan.persistence.memory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 키워드 별 검색 카운트를 위한 인메모리 데이터 구조 생성 <br>
 * 실무에서는 Redis 으로 대체하여 더 나은 메모리 관리와 확장성 제공 가능 (ex. Redis Sorted Set)
 */
@Configuration
public class InMemoryKeywordCountMap {

    /**
     * 키워드 개수를 저장하기 위한 K-V Map 을 생성
     *
     * @return 검색 키워드를 키로, 해당 키워드의 검색 횟수를 값으로 가지는 Key-Value 메모리 ({@link ConcurrentHashMap})
     */
    @Bean
    public Map<String, Integer> keywordCounts() {
        return new ConcurrentHashMap<>();
    }

}
