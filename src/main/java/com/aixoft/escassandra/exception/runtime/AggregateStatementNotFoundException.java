package com.aixoft.escassandra.exception.runtime;

/**
 * The type Aggregate statement not found exception.
 */
public class AggregateStatementNotFoundException extends RuntimeException {
    /**
     * Instantiates a new Aggregate statement not found exception.
     *
     * @param message Exception message.
     */
    public AggregateStatementNotFoundException(String message) {
        super(message);
    }
}
