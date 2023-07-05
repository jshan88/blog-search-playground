package com.jshan.persistence.redis;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestRedisServerInitializer.class)
class EmbeddedRedisTest {

    private RedisTemplate<String, Object> redisTemplate;

    @BeforeEach
    void setUp() {
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
        redisStandaloneConfiguration.setHostName("localhost");
        redisStandaloneConfiguration.setPort(6379);
        LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory(redisStandaloneConfiguration);
        connectionFactory.afterPropertiesSet();

        redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.afterPropertiesSet();
    }

    @Test
    @DisplayName("임베디드 레디스 작동 여부 테스트")
    void givenEmbeddedRedis_whenRedisTemplateMethodInvoked_thenItWorks() {

        // GIVEN TestRedisServer
        // WHEN
        redisTemplate.opsForValue().set("embedded:key", "value");

        // THEN
        assertEquals("value", redisTemplate.opsForValue().get("embedded:key"));
    }

    @Test
    @DisplayName("Concurrency_Atomicity보장_ZSet_ZINCRBY")
    void givenRedisZSet_whenConcurrentIncrementScores_thenStillAtomicityGuaranteed() throws InterruptedException {

        //GIVEN
        int threadCounts = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCounts);
        CountDownLatch countDownLatch = new CountDownLatch(threadCounts);

        String redisKey = "top-keywords";
        String keyword1 = "test1";
        String keyword2 = "test2";

        //WHEN
        for(int i = 0; i < threadCounts; i++) {
            executorService.execute(() -> {
                redisTemplate.opsForZSet().incrementScore(redisKey, keyword1, 2d);
                redisTemplate.opsForZSet().incrementScore(redisKey, keyword2, 1d);
                countDownLatch.countDown();
            });
        }

        countDownLatch.await();

        Set<TypedTuple<Object>> typedTuples = redisTemplate.opsForZSet().reverseRangeWithScores(redisKey, 0L, 1L);
        List<Double> doubles = typedTuples.stream().map(TypedTuple::getScore).toList();


        //THEN
        int keyword1Count = doubles.get(0).intValue();
        int keyword2Count = doubles.get(1).intValue();

        assertEquals(threadCounts * 2, keyword1Count);
        assertEquals(threadCounts, keyword2Count);
    }
}
