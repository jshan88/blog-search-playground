package com.jshan.controller;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jshan.dto.TopKeywordsResponse;
import com.jshan.service.SearchService;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith(SpringExtension.class)
class SearchControllerTest {

    private MockMvc mockMvc;

    @Mock
    private SearchService searchService;

    @InjectMocks
    private SearchController searchController;

    private final ObjectMapper objectMapper = new ObjectMapper();

//    @Test
//    @DisplayName("/blogs 테스트")
//    void getBlogs_ReturnsSearchResult() throws Exception {
//
//        // GIVEN
//        SearchResult expectedResult = new SearchResult();
//        when(searchService.getBlogs(any(SearchParam.class))).thenReturn(expectedResult);
//
//        // WHEN & THEN
//        mockMvc = standaloneSetup(searchController).build();
//        mockMvc.perform(get("/blogs")
//                            .param("query", "KAKAO")
//                            .param("sort", SortType.ACCURACY.toString()))
//            .andExpect(status().isOk())
//            .andExpect(content().json(objectMapper.writeValueAsString(expectedResult)));
//
//        // VERIFY
//        verify(searchService, times(1)).getBlogs(any(SearchParam.class));
//        verifyNoMoreInteractions(searchService);
//    }

    @Test
    @DisplayName("/blogs/top-keywords 테스트")
    void getPopularKeywords_ReturnsTopKeywords() throws Exception {

        // GIVEN
        List<TopKeywordsResponse> expectedResult = Arrays.asList(
            new TopKeywordsResponse("keyword1", 10),
            new TopKeywordsResponse("keyword2", 5)
        );
        when(searchService.getPopularKeywords()).thenReturn(expectedResult);

        // WHEN & THEN
        mockMvc = standaloneSetup(searchController).build();
        mockMvc.perform(get("/blogs/top-keywords"))
            .andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(expectedResult)));

        // VERIFY
        verify(searchService, times(1)).getPopularKeywords();
        verifyNoMoreInteractions(searchService);
    }
}
