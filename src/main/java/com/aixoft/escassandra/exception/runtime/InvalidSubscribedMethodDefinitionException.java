package com.aixoft.escassandra.exception.runtime;

/**
 * The type Invalid event handler definition exception.
 */
public class InvalidSubscribedMethodDefinitionException extends RuntimeException {
    /**
     * Instantiates a new Invalid event handler definition exception.
     *
     * @param message Exception message.
     */
    public InvalidSubscribedMethodDefinitionException(String message) {
        super(message);
    }
}
