package com.aixoft.escassandra.service;

import com.aixoft.escassandra.aggregate.Aggregate;
import com.aixoft.escassandra.aggregate.AggregateRoot;
import com.aixoft.escassandra.exception.checked.AggregateNotFoundException;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Aggregate Store is used to update, save and load aggregate from events with reactive approach.
 */
public interface ReactiveAggregateStore {
    /**
     * Creates Mono for saving all uncommitted events to the database.
     * <p>
     * Store is performed in following order:
     * <p>
     *     1. Events are applied on aggregate through updater (See {@link com.aixoft.escassandra.model.Event#updater()}).
     * <p>
     *     2. If event applied successfully then events are persisted in cassandra database.
     * <p>
     *     3. If no event conflict on data persist,
     *     then events are published to subscribed {@link com.aixoft.escassandra.annotation.EventListener}
     *     (See {@link com.aixoft.escassandra.annotation.SubscribeAll}).
     * <p>
     *
     * List of uncommitted events will be cleared (See {@link AggregateRoot#getUncommittedEvents()}.
     *
     * Committed and current version of the aggregate will be equal last event version
     * (See {@link AggregateRoot#getCommittedVersion()} ()} and {@link AggregateRoot#getCurrentVersion()}).
     *
     * @param aggregate Aggregate to be stored.
     * @param <T>       Aggregate data type.
     *
     * @return Mono of aggregate's committed copy if operation was successful or empty otherwise.
     */
    <T> Mono<Aggregate<T>> save(Aggregate<T> aggregate);

    /**
     * Creates Mono for restoring aggregate from events using event sourcing.
     * <p>
     * All events stored in database are used to restore aggregate.
     * <p>
     * Not effective is snapshots are being used.
     * <p>
     * Committed version of the aggregate will be equal last event version (See {@link AggregateRoot#getCommittedVersion()}).
     *
     * @param <T>                   Aggregate type.
     * @param aggregateId           UUID of the aggregate.
     * @param aggregateDataClass    Aggregate data class.
     *
     * @return Mono from restored aggregate or Mono.error({@link AggregateNotFoundException}) otherwise.
     */
    <T> Mono<Aggregate<T>> loadById(UUID aggregateId, Class<T> aggregateDataClass);

    /**
     * Creates Mono for restoring aggregate from events using event sourcing.
     * <p>
     * All events stored in database since given {@code snapshotVersion} are used to restore aggregate.
     * <p>
     * Effective if snapshots are being used.
     * <p>
     * Committed version of the aggregate will be equal last event version (See {@link AggregateRoot#getCommittedVersion()}).
     *
     * @param <T>                   Aggregate type.
     * @param aggregateId           UUID of the aggregate.
     * @param snapshotVersion       Major version of the event ({@link com.aixoft.escassandra.model.EventVersion}#getMajor()).
     * @param aggregateDataClass    Aggregate data class.
     *
     * @return Mono from restored aggregate or Mono.error({@link AggregateNotFoundException}) otherwise.
     */
    <T> Mono<Aggregate<T>> loadById(UUID aggregateId, int snapshotVersion, Class<T> aggregateDataClass);
}
