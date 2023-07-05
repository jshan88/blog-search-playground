package com.jshan.engines;

import com.jshan.config.KakaoClientProperties;
import com.jshan.dto.request.SearchParam;
import com.jshan.dto.response.Document;
import com.jshan.dto.response.SearchResult;
import com.jshan.dto.response.kakao.KakaoResponse;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * 카카오 검색엔진 클래스
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class KakaoSearchEngine extends AbstractSearchEngine {

    private final KakaoClientProperties properties;

    @Override
    public SearchResult search(SearchParam param) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, properties.getApiKey());
        URI uri = buildUri(param);

        ResponseSpec responseSpec = getResponse(headers, uri);

        return responseSpec
            .bodyToMono(KakaoResponse.class)
            .map(response -> {
                List<Document> documents = response.getDocuments()
                    .stream()
                    .map(Document::fromKakao)
                    .toList();

                return SearchResult.builder()
                    .totalCount(response.getMeta().getTotal_count())
                    .totalPage(response.getMeta().getTotal_count() / (param.getSize() == 0 ? 10 : param.getSize()))
                    .currentPage(param.getPage())
                    .documents(documents)
                    .build();
            })
            .block();
    }

    private URI buildUri(SearchParam param) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(properties.getUri())
            .queryParam("query", param.getQuery()) //param.getQuery())
            .queryParam("sort", param.getSort().value());

        if (param.getPage() != 0) {
            uriBuilder.queryParam("page", param.getPage());
        }
        if (param.getSize() != 0) {
            uriBuilder.queryParam("size", param.getSize());
        }

        return uriBuilder.build().toUri();
    }
}
