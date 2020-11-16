package com.aixoft.escassandra.exception.runtime;

/**
 * The type Aggregate annotation missing exception.
 * <p>
 * See {@link com.aixoft.escassandra.annotation.Aggregate}
 */
public class AggregateAnnotationMissingException extends RuntimeException {
    /**
     * Instantiates a new Aggregate annotation missing exception.
     *
     * @param message Exception message.
     */
    public AggregateAnnotationMissingException(String message) {
        super(message);
    }
}
