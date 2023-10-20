package com.jshan.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import com.jshan.dto.TopKeywordsResponse;
import com.jshan.dto.response.SearchResult;
import com.jshan.service.SearchService;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.bind.annotation.GetMapping;
import reactor.core.publisher.Mono;

@WebFluxTest(SearchController.class)
public class SearchControllerTest_WebFlux {

    @Autowired
    WebTestClient testClient;

    @MockBean
    SearchService searchService;

    @Test
    void test() {
        SearchResult expectedResult = SearchResult.builder().totalPage(10).build();
        when(searchService.getBlogs(any())).thenReturn(Mono.just(expectedResult));

//            .thenReturn(Mono.just(any(SearchResult.class)));

        SearchResult responseBody = testClient.get()
            .uri("/blogs?query='KAKAO'")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectBody(SearchResult.class)
            .returnResult().getResponseBody();

        Assertions.assertEquals(10, expectedResult.getTotalPage());
        Mockito.verify(searchService, times(1)).getBlogs(any());
    }

    @Test
    void test_popularkeywords() {

        List<TopKeywordsResponse> expectedList = List.of(
            TopKeywordsResponse.builder().keyword("kakao").count(10).build(),
            TopKeywordsResponse.builder().keyword("naver").count(5).build(),
            TopKeywordsResponse.builder().keyword("skt").count(4).build()
        );
        when(searchService.getPopularKeywords()).thenReturn(expectedList);

        List<TopKeywordsResponse> responseBody = testClient.get()
            .uri("/blogs/top-keywords")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectBodyList(TopKeywordsResponse.class)
            .returnResult().getResponseBody();

        Assertions.assertEquals(10, responseBody.get(0).getCount());
    }

//    @GetMapping("/blogs/top-keywords")
//    public List<TopKeywordsResponse> getPopularKeywords() {
//        return searchService.getPopularKeywords();
//    }
}
