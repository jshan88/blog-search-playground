package com.jshan.controller;

import com.jshan.dto.TopKeywordsResponse;
import com.jshan.dto.request.SearchParam;
import com.jshan.dto.response.SearchResult;
import com.jshan.service.SearchService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@RestController
public class SearchController {

    private final SearchService searchService;

    @GetMapping("/blogs")
    public Mono<SearchResult> getBlogs(@ModelAttribute SearchParam param) {
        return searchService.getBlogs(param);
    }

    @GetMapping("/blogs/top-keywords")
    public List<TopKeywordsResponse> getPopularKeywords() {
        return searchService.getPopularKeywords();
    }
}
