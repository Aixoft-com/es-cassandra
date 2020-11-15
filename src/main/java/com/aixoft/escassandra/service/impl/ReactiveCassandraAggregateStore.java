package com.aixoft.escassandra.service.impl;

import com.aixoft.escassandra.aggregate.AggregateRoot;
import com.aixoft.escassandra.component.AggregateCommitter;
import com.aixoft.escassandra.exception.checked.AggregateNotFoundException;
import com.aixoft.escassandra.exception.checked.AggregateFailedSaveException;
import com.aixoft.escassandra.repository.ReactiveEventDescriptorRepository;
import com.aixoft.escassandra.repository.model.EventDescriptor;
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
    AggregateCommitter aggregateCommitter;

    /**
     * Creates Mono for persisting all uncommitted events to the database.
     *
     * Events are applied on the aggregate (See {@link com.aixoft.escassandra.annotation.Subscribe}).
     * Events are published to subscribed {@link com.aixoft.escassandra.service.EventListener} (See {@link com.aixoft.escassandra.annotation.SubscribeAll}).
     *
     * List of uncommitted events will be cleared (See {@link AggregateRoot#getUncommittedEvents()}.
     *
     * Committed version of the aggregate will be equal last event version (See {@link AggregateRoot#getCommittedVersion()}).
     *
     * If data persistence failed then Aggregate is not updated and no message is published.
     *
     * @param aggregate Aggregate to be stored.
     * @param <T> Type of aggregate.
     * @return Mono with aggregate if operation was successful or Mono.error({@link AggregateFailedSaveException}) otherwise.
     */
    @Override
    public <T extends AggregateRoot> Mono<T> save(T aggregate) {

        List<EventDescriptor> newEventDescriptors = EventDescriptor.fromEvents(aggregate.getUncommittedEvents(), aggregate.getCommittedVersion());

        return eventDescriptorRepository.insertAll(aggregate.getClass(), aggregate.getId(), newEventDescriptors)
            .flatMap(inserted -> Boolean.TRUE.equals(inserted) ?
                Mono.fromCallable(() -> aggregateCommitter.commit(aggregate, newEventDescriptors))
                : Mono.error(AggregateFailedSaveException::new));
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
     * @param aggregateId UUID of the aggregate.
     * @param aggregateClass Class of Aggregate.
     * @param <T> Aggregate type.
     * @return Mono from restored aggregate or Mono.error({@link AggregateNotFoundException}) otherwise.
     */
    @Override
    public <T extends AggregateRoot> Mono<T> loadById(UUID aggregateId, Class<T> aggregateClass) {

         return eventDescriptorRepository.findAllByAggregateId(aggregateClass, aggregateId)
            .switchIfEmpty(Mono.error(AggregateNotFoundException::new))
            .reduceWith(
                () -> AggregateRoot.create(aggregateId, aggregateClass),
                aggregateCommitter::apply
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
     * @param aggregateId UUID of the aggregate.
     * @param snapshotVersion Major version of the event ({@link com.aixoft.escassandra.model.EventVersion#getMajor()}).
     * @param aggregateClass Class of Aggregate.
     * @param <T> Aggregate type.
     * @return Mono from restored aggregate or Mono.error({@link AggregateNotFoundException}) otherwise.
     */
    @Override
    public <T extends AggregateRoot> Mono<T> loadById(UUID aggregateId, int snapshotVersion, Class<T> aggregateClass) {
        return eventDescriptorRepository.findAllByAggregateIdSinceSnapshot(aggregateClass, aggregateId, snapshotVersion)
            .switchIfEmpty(Flux.error(AggregateNotFoundException::new))
            .reduceWith(
                () -> AggregateRoot.create(aggregateId, aggregateClass),
                aggregateCommitter::apply
            );
    }
}
