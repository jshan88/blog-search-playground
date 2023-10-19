package com.jshan.service;

import com.jshan.dto.TopKeywordsResponse;
import com.jshan.dto.request.SearchParam;
import com.jshan.dto.response.SearchResult;
import com.jshan.engines.KakaoSearchEngine;
import com.jshan.engines.NaverSearchEngine;
import com.jshan.keywordtracker.KeywordTracker;
import com.jshan.keywordtracker.factory.KeywordTrackerFactory;
import com.jshan.keywordtracker.factory.KeywordTrackerFactory.TrackerType;
import com.jshan.keywordtracker.trackers.InMemoryKeywordTracker;
import com.jshan.strategy.SearchStrategy;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * 다양한 검색 엔진을 사용하여 블로그 검색을 수행하는 서비스 클래스 <br>
 * {@link SearchStrategy}, {@link CircuitBreaker} 및 {@link InMemoryKeywordTracker}를 활용
 */
@RequiredArgsConstructor
@Service
public class SearchService {

    private final KakaoSearchEngine kakaoSearchEngine;
    private final NaverSearchEngine naverSearchEngine;
    private final CircuitBreaker circuitBreaker;
    private final InMemoryKeywordTracker inMemoryKeywordTracker;
    private final KeywordTrackerFactory keywordTrackerFactory;

    /**
     * 제공된 검색 파라미터를 기반으로 블로그 검색 결과를 가져옴
     *
     * @param param 검색 쿼리 파라미터를 담은 {@link SearchParam} 객체
     * @return {@link SearchResult} 검색 결과
     */
    public Mono<SearchResult> getBlogs(SearchParam param) {
        KeywordTracker keywordTracker = keywordTrackerFactory.createKeywordTracker(TrackerType.REDIS);
//        KeywordTracker keywordTracker = keywordTrackerFactory.createKeywordTracker(TrackerType.IN_MEMORY);

        SearchStrategy searchStrategy = new SearchStrategy(kakaoSearchEngine, naverSearchEngine);
        searchStrategy.setOnSearchListener(keywordTracker::onSearch);
        searchStrategy.setCircuitBreaker(circuitBreaker);
        return searchStrategy.searchBlogs(param);
    }

    public List<TopKeywordsResponse> getPopularKeywords() {
        KeywordTracker keywordTracker = keywordTrackerFactory.createKeywordTracker(TrackerType.REDIS);
//        KeywordTracker keywordTracker = keywordTrackerFactory.createKeywordTracker(TrackerType.IN_MEMORY);

        return keywordTracker.getPopularKeywords().stream()
                                            .map(result -> TopKeywordsResponse.builder()
                                            .keyword(result.getKeyword())
                                            .count(result.getCount())
                                            .build()).toList();
    }
}
