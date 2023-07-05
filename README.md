# Blog Search Application
[***Executable Jar 다운로드 링크***](https://github.com/jshan88/20230705_2126-000355/blob/main/app-search-1.0-SNAPSHOT-boot.jar)


- 사용자가 키워드를 기반으로 블로그를 검색할 수 있는 블로그 검색 어플리케이션입니다.
- Kakao Open API를 활용하여 블로그 검색을 수행하며, 4xx 외 에러 발생 시, Circuit Breaker 설정에 따라 Naver Open API 로 대체 수행 됩니다. 
- 또한 가장 많이 검색된 키워드 10건을 내림차순으로 제공하고 있습니다.

## 1. 기능 요구사항 (Functional Requirements)

### 1.1 블로그 검색 기능
API 명세서를 참고하여 블로그를 검색할 수 있습니다.

- **GET** /blogs : [*API 명세서*](./api-doc/API.md#get-blogs)
- 검색 대상 소스는 Interface 로 추상화 하였으며, 전략 패턴 활용하여 향후 새로운 검색 소스 추가될 시 유연하게 변경 가능합니다.

### 1.2 인기 검색어 목록 기능
API 명세서를 참고하여 인기 검색어 목록을 확인할 수 있습니다.

- **GET** /blogs/top-keywords : [*API 명세서*](./api-doc/API.md#get-blogstop-keywords)
- 동 기능은 두 가지 방법으로 구현하였습니다. 팩토리를 통해 둘 중 1개 방식을 취할 수 있습니다.
    - 첫째, 어플리케이션 메모리 (ConcurrentMap, MinMaxPriorityQueue) 에 저장하고, 시스템 리부팅 등을 고려한 DB 주기적 백업 
    - 둘째, 효율적인 메모리 관리를 위해 어플리케이션 메모리가 아닌 Redis 활용 (Sorted Set)


## 2. 우대사항 구현 내용

### 2.1 멀티 모듈 프로젝트 구성

<details>
<summary>프로젝트 구조 펼치기</summary>

```
├── app-search
│   └── src
│       ├── main
│       │   └── java
│       │       └── com.jshan
│       │           ├── SearchApplication.java
│       │           ├── controller
│       │           │   └── SearchController.java
│       │           ├── dto
│       │           │   └── TopKeywordsResponse.java
│       │           └── service
│       │               └── SearchService.java
│       └── resources
│           └── application.yml
├── persistence
│   └── src
│       ├── main
│       │   └── java
│       │       └── com.jshan.persistence
│       │           ├── KeywordCount.java
│       │           ├── database
│       │           │   ├── entity
│       │           │   │   └── TopKeyword.java
│       │           │   └── repository
│       │           │       └── TopKeywordRepository.java
│       │           ├── memory
│       │           │   ├── InMemoryKeywordCountMap.java
│       │           │   ├── InMemoryTopKeywordsQueue.java
│       │           │   └── TopKeywordsRdbCopier.java
│       │           └── redis
│       │               └── config
│       │                   ├── EmbeddedRedisInitializer.java
│       │                   └── RedisConfig.java
│       └── resources
│           └── application-persistence.yml
├── search-analytics
│   └── src
│       ├── main
│       │   └── java
│       │       └── com.jshan.keywordtracker
│       │           ├── KeywordTracker.java
│       │           ├── factory
│       │           │   └── KeywordTrackerFactory.java
│       │           └── trackers
│       │               ├── InMemoryKeywordTracker.java
│       │               └── RedisKeywordTracker.java
│       └── resources
│           └── application-analytics.yml
└── search-engine
    └── src
        ├── main
        │   └── java
        │       └── com.jshan
        │           ├── circuitbreaker
        │           │   └── CircuitConfig.java
        │           ├── config
        │           │   ├── KakaoClientProperties.java
        │           │   └── NaverClientProperties.java
        │           ├── dto
        │           │   ├── request
        │           │   │   └── SearchParam.java
        │           │   └── response
        │           │       ├── Document.java
        │           │       ├── SearchResult.java
        │           │       ├── kakao
        │           │       │   ├── KakaoDocument.java
        │           │       │   ├── KakaoMeta.java
        │           │       │   └── KakaoResponse.java
        │           │       └── naver
        │           │           ├── NaverDocument.java
        │           │           └── NaverResponse.java
        │           ├── engines
        │           │   ├── AbstractSearchEngine.java
        │           │   ├── KakaoSearchEngine.java
        │           │   └── NaverSearchEngine.java
        │           │   └── SearchEngine.java
        │           ├── exception
        │           │   ├── ApiExceptionHandler.java
        │           │   └── ApiResponseException.java
        │           └── strategy
        │               └── SearchStrategy.java
        └── resources
            └── application-engine.yml
```            
</details>

### app-search
`app-search` 모듈은 애플리케이션의 엔트리포인트로, 블로그 검색 기능을 위한 API 를 제공합니다. 
프로젝트 내의 모든 모듈들과 통합되어 검색의 전체 과정을 수행하고 처리합니다.  

<details>
<summary>[코드 확인] build.gradle</summary>

```groovy
//module dependencies
implementation project(':search-engine')
implementation project(':search-analytics')
implementation project(':persistence')
```
</details>

<details>
<summary>[코드 확인] SearchService.java</summary>

```java
public SearchResult getBlogs(SearchParam param) {
    KeywordTracker keywordTracker = keywordTrackerFactory.createKeywordTracker(TrackerType.REDIS);
    // KeywordTracker keywordTracker = keywordTrackerFactory.createKeywordTracker(TrackerType.IN_MEMORY);

    SearchStrategy searchStrategy = new SearchStrategy(kakaoSearchEngine, naverSearchEngine);
    searchStrategy.setOnSearchListener(keywordTracker::onSearch);
    searchStrategy.setCircuitBreaker(circuitBreaker);
    return searchStrategy.searchBlogs(param);
}

public List<TopKeywordsResponse> getPopularKeywords() {
    KeywordTracker keywordTracker = keywordTrackerFactory.createKeywordTracker(TrackerType.REDIS);
    // KeywordTracker keywordTracker = keywordTrackerFactory.createKeywordTracker(TrackerType.IN_MEMORY);

    return keywordTracker.getPopularKeywords().stream()
                                        .map(result -> TopKeywordsResponse.builder()
                                        .keyword(result.getKeyword())
                                        .count(result.getCount())
                                        .build()).toList();
    }
```
</details>

### persistence

`persistence` 모듈은 프로젝트에서 사용할 데이터 레이어 옵션을 구성하며, 각 데이터 레이어에 접근하기 위한 설정과 연동을 담당합니다.
데이터 레이어 옵션에는 첫째, 자바 메모리 (`ConcurrentHashMap`, `MinMaxPriorityQueue`) 내 저장하는 방법. 둘째, Redis(`Sorted Set`) 내 저장하는 방법 두가지를 구현하였으며, 실제 트래픽이 발생하는 프로덕션에서는 첫번째 방식은 적합하지 않을 수 있습니다.
첫번째 방식을 택할 시, 주기적으로 RDB (h2) 에 백업하는 Job 이 있습니다. 

<details>
<summary>[코드 확인] TopKeywordsRdbCopier.java</summary>

```java
@Component
@RequiredArgsConstructor
public class TopKeywordsRdbCopier {

    private final Queue<KeywordCount> topKeywords;
    private final TopKeywordRepository topKeywordRepository;
    private boolean isCopyingToRdb = false;

    /**
     * 일정 간격으로 인메모리 데이터를 데이터베이스로 복사 수행 (60초마다 수행)
     */
    @Scheduled(fixedDelay = 60000)
    public void copyToRdb() {
        if (isCopyingToRdb) {
            return;
        }

        synchronized (topKeywords) {
            if(!topKeywords.isEmpty()) {
                isCopyingToRdb = true;

                List<TopKeyword> keywords = topKeywords.stream()
                    .map(topKeyword -> TopKeyword.builder()
                        .keyword(topKeyword.getKeyword())
                        .count(topKeyword.getCount())
                        .build()).toList();

                topKeywordRepository.deleteAllInBatch();
                topKeywordRepository.saveAll(keywords);
                isCopyingToRdb = false;
            }
        }
    }
}
```
</details>


### search-engine

`search-engine` 모듈은 외부 검색 엔진과의 Integration을 담당합니다.
`SearchStrategy` 클래스를 사용하여 Primary Search Engine 과 Fallback Search Engine 을 설정하고, 이 둘은 `CircuitBreaker`의 상태에 따라 서로 간 전환됩니다. 
또한 이 모듈에서 `onSearchListener`(옵저버)를 설정하여 검색 이후 추가 프로세스를 설정할 수 있습니다. (예시 : 가장 많이 검색된 키워드 추적을 위한 onSearch 리스너)

<details>
<summary>[코드 확인] SearchStrategy.java</summary>

```java
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
            throw new RuntimeException(e);
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
```
</details>

### search-analytics

`search-analytics` 모듈은 애플리케이션 내에서 수행된 검색을 분석합니다.
검색 발생 시 수행 작업을 `onSearch` 메서드를 통해 구현하며, `getPopularKeywords` 메소드를 통해 가장 많이 검색된 키워드를 리턴해줍니다.
해당 두개의 메소드를 구체화한 `KeywordTracker` 인스턴스 를 `KeywordTrackerFactory`를 통해 생성합니다.

<details>
<summary>[코드 확인] KeywordTrackerFactory.java</summary>

```java
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
```
</details>

<details>
<summary>[코드 확인] InMemoryKeywordTracker</summary>

```java
/**
 * 검색 이벤트에 기반하여 키워드의 해당 키워드의 Popularity 를 추적하는 클래스 <br>
 * 키워드 검색 횟수를 업데이트하고 인기 있는 키워드 목록을 검색하는 메서드를 제공 <br>
 * 이 때 사용하는 데이터 레이어는 In-memory 이다.
 */
@Component
@RequiredArgsConstructor
public class InMemoryKeywordTracker implements KeywordTracker {

    private final Map<String, Integer> keywordCounts;
    private final Queue<KeywordCount> topKeywords;
    private final TopKeywordRepository topKeywordRepository;

    @Override
    public void onSearch(String keyword) {
        keywordCounts.merge(keyword, 1, Integer::sum);
        updateTopKeywords(keyword, keywordCounts.get(keyword));
    }

    private void updateTopKeywords(String keyword, int count) {
        topKeywords.removeIf(kc -> kc.getKeyword().equals(keyword));
        KeywordCount keywordCount = new KeywordCount(keyword, count);
        topKeywords.add(keywordCount);
    }

    /**
     * 인기 있는 키워드 목록을 검색. 키워드는 검색 횟수를 기준으로 내림차순 정렬 <br>
     * 시스템 재부팅 등의 사유로 topKeywords 가 비어있을 시, DB 로부터 다시 가져옴.
     *
     * @return Top 10 KeywordCount 객체 리스트
     */
    @Override
    public List<KeywordCount> getPopularKeywords() {
        if(topKeywords.isEmpty()) {
            List<TopKeyword> databaseTopKeywords = topKeywordRepository.findAll();
            List<KeywordCount> keywordCountList = databaseTopKeywords.stream()
                .map(topKeyword -> KeywordCount.builder()
                    .keyword(topKeyword.getKeyword())
                    .count(topKeyword.getCount())
                    .build()).toList();
            topKeywords.addAll(keywordCountList);
        }
        List<KeywordCount> popularKeywords = new ArrayList<>(topKeywords);
        popularKeywords.sort(Comparator.reverseOrder());

        return popularKeywords;
    }
}
```
</details>

<details><summary>[코드 확인] RedisKeywordTracker</summary>

```java
/**
 * 검색 이벤트에 기반하여 키워드의 해당 키워드의 Popularity 를 추적하는 클래스 <br>
 * 키워드 검색 횟수를 업데이트하고 인기 있는 키워드 목록을 검색하는 메서드를 제공 <br>
 * 이 때 사용하는 데이터 레이어는 Redis  이다.
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
```
</details>

### 2.2 Back-end 추가 요건

#### 트래픽이 많고, 저장되어 있는 데이터가 많음을 염두에 둔 구현
- 대량 트래픽 유입 시, DB Disk I/O 에 의한 병목 가능성이 있습니다. 
- 이에 In-memory 구조의 데이터 저장소가 필요했으며, 두가지 옵션을 통해 이를 구현하였습니다.
- 첫째는 애플리케이션 내 자체 메모리를 활용한 것. 단, 이 경우 1) scale-out 구조를 가져갈 수 없다는 점. 2) 시스템 리부팅 시 휘발성에 대한 플랜이 있어야 하는점. 3) 리소스 과부하에 대한 문제 야기성 을 고려해야합니다.
- 둘째로 첫번째 방법을 보완하는 Redis 의 활용입니다. 이 경우 분산 시스템, 또는 scalable 구조에서 대처가 가능합니다. 
- 본 애플리케이션은 두번째, Redis 활용이 Default 옵션입니다.

#### 동시성 이슈가 발생할 수 있는 부분을 염두에 둔 구현 (예시. 키워드 별로 검색된 횟수의 정확도)
- 애플리케이션 내 자체 메모리 활용 시, Concurrent 환경에서 Atomicity 를 보장할 수 있는 ConcurrentHashMap 을 활용하였습니다. 
- ConcurrentHashMap 의 경우 이름에서 제시하듯 atomic 을 보장합니다. 
- 아래 테스트 코드를 통해 원자성 확인을 하였습니다.

<details>
<summary>[테스트 코드] InMemoryKeywordCountMapTest.java</summary>

```java
class InMemoryKeywordCountMapTest {

  @Test
  @DisplayName("Concurrency_Atomicity보장_ConcurrentHashMap")
  void givenConcurrentHashMap_whenConcurrentMerge_thenStillAtomicityGuaranteed() throws InterruptedException {

    // GIVEN
    Map<String, Integer> keywordCounts = new ConcurrentHashMap<>();
    String keyword = "keyword";

    int threadCounts = 50;
    int executePerThread = 50;
    ExecutorService executorService = Executors.newFixedThreadPool(threadCounts);
    CountDownLatch countDownLatch = new CountDownLatch(threadCounts * executePerThread);

    // threadCounts 만큼의 쓰레드가 각각 executePerThread 만큼 merge 수행
    for(int i = 0; i < threadCounts; i++) {
      executorService.execute(() -> {
        for(int j = 0; j < executePerThread; j++) {
          keywordCounts.merge(keyword, 1, Integer::sum);
          countDownLatch.countDown();
        }
      });
    }

    countDownLatch.await();

    // THEN
    assertEquals(0, countDownLatch.getCount());
    assertEquals(threadCounts * executePerThread, keywordCounts.get(keyword));
  }
}
```
</details>

<details>
<summary>[테스트 코드]InMemoryKeywordTrackerTest.java</summary>

```java
class InMemoryKeywordTrackerTest {

  @Mock
  private TopKeywordRepository topKeywordRepository;

  @Test
  @DisplayName("Concurrency_Atomicity보장_InMemoryKeywordTracer.onSearch()")
  void givenInMemoryPersistence_whenConcurrentSearchInvoked_thenStillConcurrencySafe() throws InterruptedException {
    // GIVEN
    Map<String, Integer> keywordCounts = new ConcurrentHashMap<>();
    Queue<KeywordCount> topKeywords = Queues.synchronizedQueue(MinMaxPriorityQueue
                                                                       .orderedBy(Comparator.comparing(KeywordCount::getCount).reversed())
                                                                       .maximumSize(10)
                                                                       .create());

    InMemoryKeywordTracker keywordTracker = new InMemoryKeywordTracker(keywordCounts, topKeywords, topKeywordRepository);

    // 쓰레드 및 쓰레드 별 작업 반복 횟수 세팅
    int threadCount = 10;
    int executePerThread = 1000;
    int searchCount = 100;

    // searchCount 횟수만큼 조회 시, 1자리 랜덤한 키 생성. randomKeyWords.size() = onSearch 작업 예정 횟수
    List<String> randomKeywords = new ArrayList<>();
    for(int i = 0; i < searchCount; i++) {
      randomKeywords.add(RandomString.make(1));
    }

    // threadCount 개의 쓰레드가 executePerThread 횟수 만큼 작업 수행
    ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
    CountDownLatch countDownLatch = new CountDownLatch(threadCount * executePerThread);

    for(int i = 0; i < threadCount * executePerThread; i++) {
      executorService.execute(() -> {
        randomKeywords.forEach(keywordTracker::onSearch);
        countDownLatch.countDown();
      });
    }

    countDownLatch.await();

    // Map 에 저장된 키워드들의 검색 카운트 총 합
    int totalCounts = keywordCounts.values().stream().mapToInt(i -> i).sum();
    // Map 에 저장된 키워드 중 가장 큰 검색 카운트
    int maximumValueInMap = keywordCounts.values().stream().max(Comparator.comparingInt(Integer::intValue)).get();

    // THEN
    // ConcurrentHashMap (keywordCounts) 의 원자성 체크
    assertEquals(threadCount * executePerThread * searchCount, totalCounts);

    // Synchronous MinMaxPriorityQueue (topKeywords) 체크
    assertEquals(maximumValueInMap, topKeywords.peek().getCount());
  }
}
```
</details>

<details>
<summary>[테스트 코드] EmbeddedRedisTest.java</summary>

```java
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

    // ...

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
        List<Double> doubles = typedTuples.stream().map(tuple -> tuple.getScore()).toList();


        //THEN
        int keyword1Count = doubles.get(0).intValue();
        int keyword2Count = doubles.get(1).intValue();

        assertEquals(threadCounts * 2, keyword1Count);
        assertEquals(threadCounts * 1, keyword2Count);
    }
}
```
</details>

#### 카카오 블로그 검색 API에 장애가 발생한 경우, 네이버 블로그 검색 API 를 통해 데이터 제공
- 규모있는 플랫폼 애플리케이션, 엔터프라이즈 레벨의 사내 IT 환경에서는 다양한 시스템 간의 Integration 을 통해 비즈니스를 해결합니다. 
- 따라서 특정 1개 시스템의 장애가 복잡한 생태계 전체에 영향을 줄 수 있으며, 이에 대한 백업 방안은 매우 중요합니다. 
- `search-engine` 모듈 내 `SearchStrategy` 에서 소개 하였듯이, resilience4j 의 `Circut Breaker` 를 통해 통신하는 Primary Search Engine (카카오) 장애 발생하면
Fallback Search Engine (네이버) 를 통해 기존 기능을 제공할 수 있도록 구현하였습니다. 
- 다음은 카카오 장애 상황을 가정하고, Circuit Breaker 를 작동시켜 실제 Fallback 엔진인 네이버로 재라우팅 됨을 확인한 테스트 코드입니다. 

<details>
<summary>[테스트 코드] 가용성테스트_CircuitBreaker_카카오to네이버</summary>

```java
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
```
</details>

