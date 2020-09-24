package com.aixoft.escassandra.exception.runtime;

public class EventHandlerInvocationFailedException extends RuntimeException {
    public EventHandlerInvocationFailedException(String message) {
        super(message);
    }
}
