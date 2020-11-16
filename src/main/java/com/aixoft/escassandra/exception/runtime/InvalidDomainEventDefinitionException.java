package com.aixoft.escassandra.exception.runtime;

/**
 * The type Invalid domain event definition exception.
 * <p>
 * See {@link com.aixoft.escassandra.annotation.DomainEvent}.
 */
public class InvalidDomainEventDefinitionException extends RuntimeException {
    /**
     * Instantiates a new Invalid event handler definition exception.
     *
     * @param message Exception message.
     */
    public InvalidDomainEventDefinitionException(String message) {
        super(message);
    }
}
