package com.jshan.engines;

import com.jshan.config.NaverClientProperties;
import com.jshan.dto.request.SearchParam;
import com.jshan.dto.request.SortType;
import com.jshan.dto.response.Document;
import com.jshan.dto.response.SearchResult;
import com.jshan.dto.response.naver.NaverResponse;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

/**
 * 네이버 검색엔진 클래스
 */
@RequiredArgsConstructor
@Component
public class NaverSearchEngine extends AbstractSearchEngine {

    private static final String CLIENT_ID = "X-Naver-Client-Id";
    private static final String CLIENT_SECRET = "X-Naver-Client-Secret";
    private final NaverClientProperties properties;

    @Override
    public Mono<SearchResult> search(SearchParam param) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(CLIENT_ID, properties.getClientId());
        headers.add(CLIENT_SECRET, properties.getClientSecret());
        URI uri = buildUri(param);

        ResponseSpec responseSpec = getResponse(headers, uri);
        return responseSpec
            .bodyToMono(NaverResponse.class)
            .map(response -> {
                List<Document> documents = response.getItems()
                    .stream()
                    .map(Document::fromNaver)
                    .toList();

                return SearchResult.builder()
                    .totalCount(response.getTotal())
                    .totalPage(response.getTotal() / (param.getSize() == 0 ? 10 : param.getSize()))
                    .currentPage(param.getPage())
                    .documents(documents)
                    .build();
            });
    }

    private URI buildUri(SearchParam param) {
        String sortType = getSortType(param);
        String query = encodeQuery(param);

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(properties.getUri())
                                            .queryParam("query", query)
                                            .queryParam("sort", sortType);

        if (param.getPage() != 0) {
            uriBuilder.queryParam("start", param.getPage());
        }
        if (param.getSize() != 0) {
            uriBuilder.queryParam("display", param.getSize());
        }

        return uriBuilder.build().toUri();
    }

    private String encodeQuery(SearchParam param) {
        String query = null;
        if (param != null && param.getQuery() != null) {
            query = URLEncoder.encode(param.getQuery(), StandardCharsets.UTF_8);
        }
        return query;
    }

    private String getSortType(SearchParam param) {
        return param.getSort().equals(SortType.ACCURACY) ? "sim" : "date";
    }
}
