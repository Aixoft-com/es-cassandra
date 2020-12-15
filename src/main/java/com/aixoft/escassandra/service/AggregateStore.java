package com.aixoft.escassandra.service;

import com.aixoft.escassandra.aggregate.AggregateRoot;

import java.util.Optional;
import java.util.UUID;

/**
 * Aggregate Store is used to update, save and load aggregate from events.
 */
public interface AggregateStore {
    /**
     * Persists all uncommitted events to the database.
     * <p>
     * Events are applied on the aggregate (See {@link com.aixoft.escassandra.annotation.Subscribe}).
     * Events are published to subscribed {@link com.aixoft.escassandra.annotation.EventListener} (See {@link com.aixoft.escassandra.annotation.SubscribeAll}).
     * <p>
     * List of uncommitted events will be cleared (See {@link AggregateRoot#getUncommittedEvents()}.
     * <p>
     * Committed version of the aggregate will be equal last event version (See {@link AggregateRoot#getCommittedVersion()}).
     * <p>
     * If data persistence failed then Aggregate is not updated and no message is published.
     *
     * @param <T>       Type of aggregate.
     * @param aggregate Aggregate to be stored.
     *
     * @return Aggregate if operation was successful or empty otherwise.
     */
    <T extends AggregateRoot> Optional<T> save(T aggregate);

    /**
     * Restores aggregate from events using event sourcing.
     * <p>
     * All events stored in database are used to restore aggregate.
     * <p>
     * Not effective is snapshots are being used.
     * <p>
     * Committed version of the aggregate will be equal last event version (See {@link AggregateRoot#getCommittedVersion()}).
     *
     * @param <T>            Aggregate type.
     * @param aggregateId    UUID of the aggregate.
     * @param aggregateClass Aggregate class.
     *
     * @return Restored aggregate or empty if aggregate not found.
     */
    <T extends AggregateRoot> Optional<T> loadById(UUID aggregateId, Class<T> aggregateClass);

    /**
     * Restores aggregate from events using event sourcing.
     * <p>
     * All events stored in database since given {@code snapshotVersion} are used to restore aggregate.
     * <p>
     * Effective if snapshots are being used.
     * <p>
     * Committed version of the aggregate will be equal last event version (See {@link AggregateRoot#getCommittedVersion()}).
     *
     * @param <T>             Aggregate type.
     * @param aggregateId     UUID of the aggregate.
     * @param snapshotVersion Major version of the event ({@link com.aixoft.escassandra.model.EventVersion#getMajor()}).
     * @param aggregateClass  Aggregate class.
     *
     * @return Restored aggregate or empty if aggregate not found.
     */
    <T extends AggregateRoot> Optional<T> loadById(UUID aggregateId, int snapshotVersion, Class<T> aggregateClass);
}
