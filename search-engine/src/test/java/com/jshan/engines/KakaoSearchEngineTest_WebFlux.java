package com.jshan.engines;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.jshan.config.KakaoClientProperties;
import com.jshan.dto.request.SearchParam;
import com.jshan.dto.request.SortType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class KakaoSearchEngineTest_WebFlux {

    @Mock
    private KakaoClientProperties properties;

    @InjectMocks
    private KakaoSearchEngine kakaoSearchEngine;

    @BeforeEach
    void setUp() {
        kakaoSearchEngine = new KakaoSearchEngine(properties);
    }

    @Test
    @DisplayName("카카오 API 호출")
    void givenSearchParam_whenSearchInvoked_thenKakaoApiShouldBeInvoked() {

        // GIVEN
        when(properties.getApiKey()).thenReturn("");
        when(properties.getUri()).thenReturn("https://dapi.kakao.com/v2/search/blog");
        SearchParam param = SearchParam.builder()
                            .query("카카오")
                            .sort(SortType.ACCURACY)
                            .page(1)
                            .size(5)
                            .build();
        // WHEN
        StepVerifier.create(kakaoSearchEngine.search(param))
            .expectNextMatches(result -> result.getCurrentPage() == 1 && result.getDocuments().size() == 5)
            .verifyComplete();

//        SearchResult result = kakaoSearchEngine.search(param);

        // THEN
//        assertEquals(5, result.getDocuments().size());
    }
}