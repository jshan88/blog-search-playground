package com.jshan.controller;

import static org.mockito.ArgumentMatchers.any;

import com.jshan.dto.response.SearchResult;
import com.jshan.service.SearchService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

/**
 * !!!Description here
 *
 * @author : jshan
 * @created : 2023/07/18
 */

@WebFluxTest(SearchController.class)
public class SearchControllerTest_Webflux {

    @Autowired
    WebTestClient webTestClient;

    @MockBean
    SearchService searchService;

    @Test
    void test() {

        SearchResult result = SearchResult.builder().totalPage(10).build();
        Mockito.when(searchService.getBlogs(any())).thenReturn(Mono.just(result));

        SearchResult responseBody = webTestClient.get()
            .uri("/blogs?query=kakaobank")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectBody(SearchResult.class)
            .returnResult().getResponseBody();

        Assertions.assertEquals(10, responseBody.getTotalPage());
    }
}
