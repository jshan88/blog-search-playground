package com.jshan.circuitbreaker;

import com.jshan.exception.ApiResponseException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.SlidingWindowType;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * {@link CircuitBreaker} 인스턴스를 생성하고 구성하기 위한 Configuration Class
 */
@Configuration
public class CircuitConfig {

    /**
     * 사용자 정의 기반 CircuitBreakerRegistry 생성
     *
     * @return {@link CircuitBreakerRegistry} 인스턴스
     */
    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        return CircuitBreakerRegistry.of(
                CircuitBreakerConfig.custom()
                    .slidingWindowType(SlidingWindowType.COUNT_BASED)
                    .slidingWindowSize(10)
                    .failureRateThreshold(30)
                    .recordException(throwable -> !(throwable instanceof ApiResponseException))
                    .build()
        );
    }

    /**
     * 설정된 CircuitBreakerRegistry 에서 CircuitBreaker 인스턴스 생성
     *
     * @param circuitBreakerRegistry CircuitBreakerRegistry 인스턴스
     * @return {@link CircuitBreaker} 인스턴스
     */
    @Bean
    public CircuitBreaker circuitBreaker(CircuitBreakerRegistry circuitBreakerRegistry) {
        return circuitBreakerRegistry.circuitBreaker("blogCircuit");
    }
}
