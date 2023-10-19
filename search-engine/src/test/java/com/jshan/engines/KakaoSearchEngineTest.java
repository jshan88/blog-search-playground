package com.jshan.engines;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.jshan.config.KakaoClientProperties;
import com.jshan.dto.request.SearchParam;
import com.jshan.dto.request.SortType;
import com.jshan.dto.response.SearchResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class KakaoSearchEngineTest {

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
        SearchResult result = kakaoSearchEngine.search(param);

        // THEN
        assertEquals(5, result.getDocuments().size());
    }
}
