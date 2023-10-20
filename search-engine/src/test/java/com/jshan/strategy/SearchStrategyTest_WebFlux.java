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
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class SearchStrategyTest_WebFlux {

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

        SearchResult expectedResult = SearchResult.builder().totalPage(10).build();

        when(primarySearchEngine.search(any())).thenReturn(Mono.error(intentional));
        when(fallbackSearchEngine.search(any())).thenReturn(Mono.just(expectedResult));

        // 10 회 이상 Intentional Exception 발생 (WebClientResponseException)
        for(int i = 0; i < 10; i++) {
            StepVerifier.create(searchStrategy.searchBlogs(any()))
                .expectError(intentional.getClass())
                .verify();
        }

        // THEN
        // Circuit Breaker OPEN 여부 확인
        assertEquals(CircuitBreaker.State.OPEN, circuitBreaker.getState());

        StepVerifier.create(searchStrategy.searchBlogs(any()))
            .expectNext(expectedResult)
                .verifyComplete();

        // Fallback Search Engine 호출 여부 확인
        verify(fallbackSearchEngine, atLeastOnce()).search(any());
    }
}