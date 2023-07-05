package com.jshan.keywordtracker.factory;

import com.jshan.keywordtracker.KeywordTracker;
import com.jshan.keywordtracker.trackers.InMemoryKeywordTracker;
import com.jshan.keywordtracker.trackers.RedisKeywordTracker;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * 지정된 {@link TrackerType} 에 기반하여 KeywordTracker 인스턴스를 생성
 */
@Component
public class KeywordTrackerFactory {

    public enum TrackerType {
        IN_MEMORY,
        REDIS
    }

    private final Map<TrackerType, KeywordTracker> trackerMap;

    /**
     * {@link KeywordTracker} 를 구현한 인스턴스를 주입 받아 KeywordTrackerFactory를 생성
     *
     * @param inMemoryTracker - {@link InMemoryKeywordTracker}
     * @param redisTracker - {@link RedisKeywordTracker}
     */
    public KeywordTrackerFactory(InMemoryKeywordTracker inMemoryTracker, RedisKeywordTracker redisTracker) {
        trackerMap = Map.of(
            TrackerType.IN_MEMORY, inMemoryTracker,
            TrackerType.REDIS, redisTracker
        );
    }

    public KeywordTracker createKeywordTracker(TrackerType trackerType) {
        return trackerMap.getOrDefault(trackerType, getDefaultTracker());
    }

    private KeywordTracker getDefaultTracker() {
        return trackerMap.get(TrackerType.REDIS);
    }
}
