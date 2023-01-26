package com.aixoft.escassandra.model;

/**
 * Each event used for event sourcing with EsCassandra shall implement this interface.
 * <p>
 * Each event shall be annotated with {@link com.aixoft.escassandra.annotation.DomainEvent} for autoconfiguration.
 */
public interface Event<T> {

    AggregateUpdater<T> updater();
}
