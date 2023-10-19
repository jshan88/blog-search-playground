package com.jshan.engines;

import com.jshan.dto.request.SearchParam;
import com.jshan.dto.response.SearchResult;
import reactor.core.publisher.Mono;

/**
 * 검색엔진 인터페이스
 */
public interface SearchEngine {

    /**
     * 주어진 검색 파라미터에 따라 검색을 수행
     *
     * @param param {@link SearchParam}
     * @return 검색 결과 {@link SearchResult}
     */
    Mono<SearchResult> search(SearchParam param);
}
