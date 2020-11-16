package com.aixoft.escassandra.service;

import com.aixoft.escassandra.aggregate.AggregateRoot;
import com.aixoft.escassandra.exception.checked.AggregateFailedSaveException;
import com.aixoft.escassandra.exception.checked.AggregateNotFoundException;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Aggregate Store is used to update, save and load aggregate from events with reactive approach.
 */
public interface ReactiveAggregateStore {
    /**
     * Creates Mono for persisting all uncommitted events to the database.
     * <p>
     * Events are applied on the aggregate (See {@link com.aixoft.escassandra.annotation.Subscribe}).
     * Events are published to subscribed {@link com.aixoft.escassandra.service.EventListener} (See {@link com.aixoft.escassandra.annotation.SubscribeAll}).
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
     * @return Mono with aggregate if operation was successful or Mono.error({@link AggregateFailedSaveException}) otherwise.
     */
    <T extends AggregateRoot> Mono<T> save(T aggregate);

    /**
     * Creates Mono for restoring aggregate from events using event sourcing.
     * <p>
     * All events stored in database are used to restore aggregate.
     * <p>
     * Not effective is snapshots are being used.
     * <p>
     * Committed version of the aggregate will be equal last event version (See {@link AggregateRoot#getCommittedVersion()}).
     *
     * @param <T>            Aggregate type.
     * @param aggregateId    UUID of the aggregate.
     * @param aggregateClass Class of Aggregate.
     *
     * @return Mono from restored aggregate or Mono.error({@link AggregateNotFoundException}) otherwise.
     */
    <T extends AggregateRoot> Mono<T> loadById(UUID aggregateId, Class<T> aggregateClass);

    /**
     * Creates Mono for restoring aggregate from events using event sourcing.
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
     * @param aggregateClass  Class of Aggregate.
     *
     * @return Mono from restored aggregate or Mono.error({@link AggregateNotFoundException}) otherwise.
     */
    <T extends AggregateRoot> Mono<T> loadById(UUID aggregateId, int snapshotVersion, Class<T> aggregateClass);
}
