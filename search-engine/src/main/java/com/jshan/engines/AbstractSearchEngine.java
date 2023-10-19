package com.jshan.engines;

import com.jshan.exception.ApiResponseException;
import java.net.URI;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;
import reactor.core.publisher.Mono;

/**
 * 추상 검색엔진을 클래스 <br>
 * 이 클래스는 SearchEngine 인터페이스를 구현하며, 검색엔진의 공통 동작을 구현함 <br>
 * 구체적인 검색엔진은 이 클래스를 상속하여 구현
 */
@Slf4j
public abstract class AbstractSearchEngine implements SearchEngine {

    /**
     * 주어진 헤더와 URI로부터 응답을 가져옴
     *
     * @param headers 요청 헤더
     * @param uri     요청 URI
     * @return 응답 결과
     */
    protected ResponseSpec getResponse(HttpHeaders headers, URI uri) {
        WebClient webClient = WebClient.create();
        return webClient.get()
            .uri(uri)
            .headers(h -> h.addAll(headers))
            .retrieve()
            ;
//            .onStatus(
//                HttpStatusCode::is4xxClientError,
//                clientResponse -> clientResponse.bodyToMono(String.class)
//                    .flatMap(errorBody -> Mono.error(new ApiResponseException(clientResponse.statusCode(), errorBody)))
//            );
    }
}
