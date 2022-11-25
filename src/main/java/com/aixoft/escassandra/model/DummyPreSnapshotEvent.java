package com.aixoft.escassandra.model;

import com.aixoft.escassandra.annotation.DomainEvent;

/**
 * Dummy event published before each snapshot event on snapshot command
 * to prevent race condition on snapshot persistence.
 * <p>
 * Persistent event will have 'BeforeSnapshotCreated' name.
 */
@DomainEvent(event = "BeforeSnapshotCreated")
public class DummyPreSnapshotEvent implements Event{
    @Override
    public AggregateUpdater<?> updater() {
        return obj -> obj;
    }
}
