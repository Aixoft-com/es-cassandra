package com.aixoft.escassandra.exception.runtime;

/**
 * The type Aggregate creation exception.
 */
public class AggregateCreationException extends RuntimeException {
    /**
     * Instantiates a new Aggregate creation exception.
     *
     * @param message   Exception message.
     * @param cause     Exception cause.
     */
    public AggregateCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}
