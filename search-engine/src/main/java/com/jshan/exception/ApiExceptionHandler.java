package com.jshan.exception;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * {@link ApiResponseException} 처리하는 핸들러
 */
@ControllerAdvice
public class ApiExceptionHandler {

    /**
     * API 예외 처리 메서드<br>
     * `ApiResponseException` 예외 발생 시, Http Status 와 원인을 포함한 응답 리턴
     *
     * @param e {@link ApiResponseException}
     * @return ResponseEntity
     */
    @ExceptionHandler(value = {ApiResponseException.class})
    public ResponseEntity<String> handleApiRequestException(ApiResponseException e) {
        return ResponseEntity.status(e.getStatus())
            .contentType(MediaType.APPLICATION_JSON)
            .body(e.getReason());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<String>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {

        List<String> errors = e.getBindingResult()
                                    .getFieldErrors()
                                    .stream()
                                    .map(error -> String.join(" : ", error.getField(), error.getDefaultMessage()))
                                    .toList();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .contentType(MediaType.APPLICATION_JSON)
            .body(errors);
    }
}