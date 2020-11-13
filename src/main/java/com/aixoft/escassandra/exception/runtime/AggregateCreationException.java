package com.aixoft.escassandra.exception.runtime;

public class AggregateCreationException extends RuntimeException {
    public AggregateCreationException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
