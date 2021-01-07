package com.aixoft.escassandra.exception.runtime;

import com.aixoft.escassandra.annotation.AggregateData;

/**
 * The type Aggregate annotation invalid format exception.
 *
 * See {@link AggregateData}.
 */
public class AggregateAnnotationInvalidFormatException extends RuntimeException {
    /**
     * Instantiates a exception indicating invalid format of aggregate annotation.
     *
     * @param message Exception message.
     */
    public AggregateAnnotationInvalidFormatException(String message) {
        super(message);
    }
}
