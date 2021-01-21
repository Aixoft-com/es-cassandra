package com.aixoft.escassandra.service.impl;

import com.aixoft.escassandra.aggregate.Aggregate;
import com.aixoft.escassandra.aggregate.AggregateRoot;
import com.aixoft.escassandra.exception.checked.AggregateNotFoundException;
import com.aixoft.escassandra.repository.ReactiveEventDescriptorRepository;
import com.aixoft.escassandra.repository.model.EventDescriptor;
import com.aixoft.escassandra.service.EventRouter;
import com.aixoft.escassandra.service.ReactiveAggregateStore;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

/**
 * Cassandra Aggregate Store is used to update, save and load aggregate from events with reactive approach.
 */
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Slf4j
public class ReactiveCassandraAggregateStore implements ReactiveAggregateStore {
    ReactiveEventDescriptorRepository eventDescriptorRepository;
    EventRouter eventRouter;

    /**
     * Creates Mono for saving all uncommitted events to the database.
     * <p>
     * Store is performed in following order:
     * <p>
     *     1. Events are applied on aggregate through updater (See {@link com.aixoft.escassandra.model.Event#createUpdater()}).
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
    @Override
    public <T> Mono<Aggregate<T>> save(Aggregate<T> aggregate) {
        Aggregate<T> newAggregate = aggregate.committedCopy();

        List<EventDescriptor> eventDescriptors = aggregate.getUncommittedEvents();

        return eventDescriptorRepository.insertAll(newAggregate.getData().getClass(), newAggregate.getId(), eventDescriptors)
            .doOnNext(eventDescriptor -> eventRouter.publish(
                eventDescriptor.getEvent(),
                eventDescriptor.getEventVersion(),
                newAggregate.getId()))
            .last()
            .switchIfEmpty(Mono.empty())
            .map(eventDescriptor -> newAggregate);
    }
    /**
     * Creates Mono for restoring aggregate from events using event sourcing.
     *
     * All events stored in database are used to restore aggregate.
     *
     * Not effective is snapshots are being used.
     *
     * Committed version of the aggregate will be equal last event version (See {@link AggregateRoot#getCommittedVersion()}).
     *
     * @param aggregateId           UUID of the aggregate.
     * @param aggregateDataClass    Aggregate data class.
     * @param <T>                   Aggregate data type.
     *
     * @return Mono from restored aggregate or Mono.error({@link AggregateNotFoundException}) otherwise.
     */
    @Override
    public <T> Mono<Aggregate<T>> loadById(UUID aggregateId, Class<T> aggregateDataClass) {
         return Aggregate.restoreFromEvents(
             aggregateId,
             eventDescriptorRepository.findAllByAggregateId(aggregateDataClass, aggregateId)
                .switchIfEmpty(Mono.error(AggregateNotFoundException::new))
         );
    }

    /**
     * Creates Mono for restoring aggregate from events using event sourcing.
     *
     * All events stored in database since given {@code snapshotVersion} are used to restore aggregate.
     *
     * Effective if snapshots are being used.
     *
     * Committed version of the aggregate will be equal last event version (See {@link AggregateRoot#getCommittedVersion()}).
     *
     * @param aggregateId           UUID of the aggregate.
     * @param snapshotVersion       Major version of the event ({@link com.aixoft.escassandra.model.EventVersion#getMajor()}).
     * @param aggregateDataClass    Aggregate data class.
     * @param <T>                   Aggregate data type.
     * @return Mono from restored aggregate or Mono.error({@link AggregateNotFoundException}) otherwise.
     */
    @Override
    public <T> Mono<Aggregate<T>> loadById(UUID aggregateId, int snapshotVersion, Class<T> aggregateDataClass) {
        return Aggregate.restoreFromEvents(
                    aggregateId,
                    eventDescriptorRepository.findAllByAggregateIdSinceSnapshot(aggregateDataClass, aggregateId, snapshotVersion)
                        .switchIfEmpty(Flux.error(AggregateNotFoundException::new))
        );
    }
}
