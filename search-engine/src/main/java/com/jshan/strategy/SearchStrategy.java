package com.jshan.strategy;

import com.jshan.circuitbreaker.CircuitBreakerException;
import com.jshan.dto.request.SearchParam;
import com.jshan.dto.response.SearchResult;
import com.jshan.engines.SearchEngine;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 기본 검색 엔진(primarySearchEngine)과 대체 검색 엔진(fallbackSearchEngine) 을 활용<br>
 * {@link CircuitBreaker} 에 따라 알맞은 검색 엔진을 사용하며, 검색 후 {@link OnSearchListener}를 호출
 */
@Slf4j
@RequiredArgsConstructor
public class SearchStrategy {

    private final SearchEngine primarySearchEngine;
    private final SearchEngine fallbackSearchEngine;
    private CircuitBreaker circuitBreaker;
    private OnSearchListener onSearchListener;

    /**
     * 검색 이벤트 발생에 따른 후속 처리 Listener 세팅.
     * 검색 성공 후, Hit Count 업데이트를 위함
     *
     * @param listener 설정할 {@link OnSearchListener}
     */
    public void setOnSearchListener(OnSearchListener listener) {
        this.onSearchListener = listener;
    }

    /**
     * Circuit Breaker 세팅
     *
     * @param circuitBreaker {@link CircuitBreaker}
     */
    public void setCircuitBreaker(CircuitBreaker circuitBreaker) {
        this.circuitBreaker = circuitBreaker;
    }

    /**
     * 주어진 검색 파라미터를 사용하여 블로그를 검색 <br>
     * 기본 검색 엔진(primarySearchEngine) 을 사용하며, Circuit Breaker 작동 시, 대체 검색 엔진(fallbackSearchEngine) 으로 전환됨.
     *
     * @param param 검색 쿼리 파라미터 {@link SearchParam}
     * @return 검색 결과 {@link SearchResult}
     * @throws RuntimeException 검색 도중 오류 발생
     */
    public SearchResult searchBlogs(SearchParam param) {
        SearchResult result;
        try {
            result = circuitBreaker.executeCallable(() -> primarySearchEngine.search(param));
        } catch (CallNotPermittedException e) {
            log.info("Primary Search Engine is not callable : {}. Switched to the Fallback Search Engine.", e.getMessage());
            result = fallbackSearchEngine.search(param);
        } catch (Exception e) {
            log.warn("The number of failed calls : {}", circuitBreaker.getMetrics().getNumberOfFailedCalls());
            throw new CircuitBreakerException(e.getMessage(), e.getCause());
        }

        if (result != null && onSearchListener != null) {
            onSearchListener.onSearch(param.getQuery());
        }

        return result;
    }

    /**
     * 검색 이벤트 발생 시, 후속 처리 리스너 인터페이스 <br>
     * 검색 수행 후 알림을 받으려면 해당 인터페이스를 구현.
     */
    public interface OnSearchListener {

        /**
         * 검색 수행 시 호출
         *
         * @param query 검색 쿼리
         */
        void onSearch(String query);
    }
}
