package com.aixoft.escassandra.exception.runtime;

/**
 * The type Event handler invocation failed exception.
 */
public class EventHandlerInvocationFailedException extends RuntimeException {
    /**
     * Instantiates a new Event handler invocation failed exception.
     *
     * @param message Exception message.
     */
    public EventHandlerInvocationFailedException(String message) {
        super(message);
    }
}
