package com.restaurant.recommender.exception;

public class CatalogNotReadyException extends IllegalStateException {
    public CatalogNotReadyException() {
        super("Restaurant catalog has not been initialized yet");
    }
}
