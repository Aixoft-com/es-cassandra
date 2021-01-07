package com.aixoft.escassandra.model;

/**
 * Event indicating snapshot.
 * <p>
 * Snapshot event can be used to improve performance of aggregate loading.
 * <p>
 * Each snapshot event used for event sourcing with EsCassandra shall implement this interface.
 * <p>
 * Each event shall be annotated with {@link com.aixoft.escassandra.annotation.DomainEvent} for autoconfiguration.
 */
public interface SnapshotEvent<T> extends Event<T> {
}
