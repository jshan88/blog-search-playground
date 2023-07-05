package com.jshan.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.jshan.dto.response.SearchResult;
import com.jshan.engines.SearchEngine;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.SlidingWindowType;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.netty.handler.timeout.TimeoutException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@ExtendWith(MockitoExtension.class)
class SearchStrategyTest {

    @Mock
    private SearchEngine primarySearchEngine;
    @Mock
    private SearchEngine fallbackSearchEngine;

    private CircuitBreaker circuitBreaker;

    @InjectMocks
    private SearchStrategy searchStrategy;

    @BeforeEach
    void setup() {
        CircuitBreakerRegistry circuitBreakerRegistry = CircuitBreakerRegistry.of(
                                                            CircuitBreakerConfig.custom()
                                                                .slidingWindowType(SlidingWindowType.COUNT_BASED)
                                                                .slidingWindowSize(10)
                                                                .failureRateThreshold(30)
                                                                .recordExceptions(WebClientException.class, TimeoutException.class)
                                                                .build());

        circuitBreaker = circuitBreakerRegistry.circuitBreaker("blogCircuit");
        searchStrategy = new SearchStrategy(primarySearchEngine, fallbackSearchEngine);
        searchStrategy.setCircuitBreaker(circuitBreaker);
    }

    @Test
    @DisplayName("가용성테스트_CircuitBreaker_카카오to네이버")
    void givenCircuitBreakerOpen_whenFallbackSearchEngineUsed_thenVerifyFallbackSearchInvoked() {

        // GIVEN+WHEN
        WebClientResponseException intentional = new WebClientResponseException("*** Intentional ****",
            HttpStatusCode.valueOf(500).value(),
            HttpStatus.INTERNAL_SERVER_ERROR.toString(), null, null, null);
        when(primarySearchEngine.search(any())).thenThrow(intentional);

        when(fallbackSearchEngine.search(any())).thenReturn(SearchResult.builder().build());

        // 10 회 이상 Intentional Exception 발생 (WebClientResponseException)
        for (int i = 1; i <= 11; i++) {
            try {
                searchStrategy.searchBlogs(any());
            } catch (Exception e) {
            }
        }

        // THEN
        // Circuit Breaker OPEN 여부 확인
        assertEquals(CircuitBreaker.State.OPEN, circuitBreaker.getState());
        // Fallback Search Engine 호출 여부 확인
        verify(fallbackSearchEngine, atLeastOnce()).search(any());
    }
}