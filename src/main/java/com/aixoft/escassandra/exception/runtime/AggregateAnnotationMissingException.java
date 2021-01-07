package com.aixoft.escassandra.exception.runtime;

import com.aixoft.escassandra.annotation.AggregateData;

/**
 * The type Aggregate annotation missing exception.
 * <p>
 * See {@link AggregateData}
 */
public class AggregateAnnotationMissingException extends RuntimeException {
    /**
     * Instantiates a exception indicating missing aggregate annotation.
     *
     * @param message Exception message.
     */
    public AggregateAnnotationMissingException(String message) {
        super(message);
    }
}
