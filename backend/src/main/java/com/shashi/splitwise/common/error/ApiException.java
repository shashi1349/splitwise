package com.shashi.splitwise.common.error;

import org.springframework.http.HttpStatus;

/**
 * Base for domain-thrown exceptions that map to HTTP problem responses.
 * The {@link com.shashi.splitwise.common.error.GlobalExceptionHandler}
 * converts these into RFC 7807 ProblemDetail bodies.
 */
public abstract class ApiException extends RuntimeException {

    private final HttpStatus status;

    protected ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
