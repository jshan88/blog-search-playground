package com.jshan.keywordtracker.trackers;

import com.jshan.keywordtracker.KeywordTracker;
import com.jshan.persistence.KeywordCount;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Component;

/**
 * Redis 사용하여 구현한 KeywordTracker
 */
@Component
@RequiredArgsConstructor
public class RedisKeywordTracker implements KeywordTracker {

    private static final String TOP_KEYWORDS = "top-keywords";
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void onSearch(String keyword) {
        redisTemplate.opsForZSet().incrementScore(TOP_KEYWORDS, keyword, 1d);
    }

    @Override
    public List<KeywordCount> getPopularKeywords() {
        Set<TypedTuple<String>> tuples = redisTemplate.opsForZSet().reverseRangeWithScores(TOP_KEYWORDS, 0L, 9L);
        if (tuples == null) {
            throw new IllegalStateException();
        }
        return tuples.stream()
                        .map(tuple -> KeywordCount.builder()
                                                .keyword(tuple.getValue())
                                                .count(tuple.getScore().intValue())
                                                .build())
                        .toList();
    }
}
