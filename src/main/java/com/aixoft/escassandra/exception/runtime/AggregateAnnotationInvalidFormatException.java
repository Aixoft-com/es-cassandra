package com.aixoft.escassandra.exception.runtime;

/**
 * The type Aggregate annotation invalid format exception.
 *
 * See {@link com.aixoft.escassandra.annotation.Aggregate}.
 */
public class AggregateAnnotationInvalidFormatException extends RuntimeException {
    /**
     * Instantiates a new Aggregate annotation invalid format exception.
     *
     * @param message Exception message.
     */
    public AggregateAnnotationInvalidFormatException(String message) {
        super(message);
    }
}
