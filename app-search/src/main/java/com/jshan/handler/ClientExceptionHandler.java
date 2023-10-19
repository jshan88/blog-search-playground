package com.jshan.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * !!!Description here
 *
 * @author : jshan
 * @created : 2023/07/18
 */
@Component
@RequiredArgsConstructor
public class ClientExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;
    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {

        ResponseTemplate template;
        ServerHttpResponse response = exchange.getResponse();
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        if(ex instanceof WebClientResponseException clientResponseException) {
            template = new ResponseTemplate<>(clientResponseException.getStatusText(), clientResponseException.getResponseBodyAsString());
            response.setStatusCode(clientResponseException.getStatusCode());
        } else if(ex instanceof WebExchangeBindException bindException) {
            List<String> errorList = bindException.getFieldErrors()
                .stream().map(err -> String.join(":", err.getField(), err.getDefaultMessage()))
                .toList();
            template = new ResponseTemplate<>(HttpStatus.BAD_REQUEST.name(), errorList);
            response.setStatusCode(HttpStatus.BAD_REQUEST);
        } else {
            template = new ResponseTemplate<>(HttpStatus.INTERNAL_SERVER_ERROR.name(), "");
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return response.writeWith(Mono.fromSupplier(() -> {
            try {
                return response.bufferFactory().wrap(objectMapper.writeValueAsBytes(template));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e); // TODO: change it to the dedicated exception later.
            }
        }));

    }
}
