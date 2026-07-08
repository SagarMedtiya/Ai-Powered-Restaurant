package com.restaurant.recommender.exception;

public class LlmServiceException extends RuntimeException {
    public LlmServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
