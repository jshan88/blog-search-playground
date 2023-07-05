package com.jshan.exception;

import lombok.Getter;
import org.springframework.http.HttpStatusCode;

@Getter
public class ApiResponseException extends RuntimeException {

    private final int status;
    private final String reason;

    public ApiResponseException(HttpStatusCode status, String reason) {
        this.status = status.value();
        this.reason = reason;
    }
}
