package com.jshan.engines;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.jshan.config.NaverClientProperties;
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
class NaverSearchEngineTest {

    @Mock
    private NaverClientProperties properties;

    @InjectMocks
    private NaverSearchEngine naverSearchEngine;

    @BeforeEach
    void setUp() {
        naverSearchEngine = new NaverSearchEngine(properties);
    }

    @Test
    @DisplayName("네이버 API 호출")
    void givenSearchParam_whenSearchInvoked_thenNaverApiShouldBeInvoked() {

        // GIVEN
        when(properties.getClientId()).thenReturn("");
        when(properties.getClientSecret()).thenReturn("");
        when(properties.getUri()).thenReturn("https://openapi.naver.com/v1/search/blog.json");

        SearchParam param = SearchParam.builder()
            .query("네이버")
            .sort(SortType.ACCURACY)
            .page(1)
            .size(5)
            .build();

        // WHEN
//        SearchResult result = naverSearchEngine.search(param);
//
//        // THEN
//        assertEquals(5, result.getDocuments().size());
    }
}
