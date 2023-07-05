package com.jshan.circuitbreaker;

public class CircuitBreakerException extends RuntimeException {

    public CircuitBreakerException() {
        super();
    }

    public CircuitBreakerException(String message) {
        super(message);
    }

    public CircuitBreakerException(Throwable cause) {
        super(cause);
    }

    public CircuitBreakerException(String message, Throwable cause) {
        super(message, cause);
    }
}
