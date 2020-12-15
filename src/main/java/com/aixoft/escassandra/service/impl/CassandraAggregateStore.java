package com.aixoft.escassandra.service.impl;

import com.aixoft.escassandra.aggregate.AggregateRoot;
import com.aixoft.escassandra.component.AggregateCommitter;
import com.aixoft.escassandra.repository.EventDescriptorRepository;
import com.aixoft.escassandra.repository.model.EventDescriptor;
import com.aixoft.escassandra.service.AggregateStore;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Cassandra Aggregate Store is used to update, save and load aggregate from events.
 */
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Slf4j
public class CassandraAggregateStore implements AggregateStore {
    EventDescriptorRepository eventDescriptorRepository;
    AggregateCommitter aggregateCommitter;

    /**
     * Persists all uncommitted events to the database.
     *
     * Events are applied on the aggregate (See {@link com.aixoft.escassandra.annotation.Subscribe}).
     * Events are published to subscribed {@link com.aixoft.escassandra.annotation.EventListener} (See {@link com.aixoft.escassandra.annotation.SubscribeAll}).
     *
     * List of uncommitted events will be cleared (See {@link AggregateRoot#getUncommittedEvents()}.
     *
     * Committed version of the aggregate will be equal last event version (See {@link AggregateRoot#getCommittedVersion()}).
     *
     * If data persistence failed then Aggregate is not updated and no message is published.
     *
     * @param aggregate Aggregate to be stored.
     * @param <T> Type of aggregate.
     * @return Aggregate if operation was successful or empty otherwise.
     */
    @Override
    public <T extends AggregateRoot> Optional<T> save(T aggregate) {
        List<EventDescriptor> newEventDescriptors = EventDescriptor.fromEvents(aggregate.getUncommittedEvents(), aggregate.getCommittedVersion());

        boolean insertSucceed = eventDescriptorRepository.insertAll(aggregate.getClass(), aggregate.getId(), newEventDescriptors);

        Optional<T> result;
        if(insertSucceed) {
            result = Optional.of(aggregateCommitter.commit(aggregate, newEventDescriptors));
        } else {
            result = Optional.empty();
        }

        return result;
    }

    /**
     * Restores aggregate from events using event sourcing.
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
     * @return Restored aggregate or empty if aggregate not found.
     */
    @Override
    public <T extends AggregateRoot> Optional<T> loadById(UUID aggregateId, Class<T> aggregateClass) {
        List<EventDescriptor> eventDescriptors = eventDescriptorRepository.findAllByAggregateId(aggregateClass, aggregateId);

        return loadFromEventDescriptors(aggregateId, aggregateClass, eventDescriptors);
    }

    /**
     * Restores aggregate from events using event sourcing.
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
     * @return Restored aggregate or empty if aggregate not found.
     */
    @Override
    public <T extends AggregateRoot> Optional<T> loadById(UUID aggregateId, int snapshotVersion, Class<T> aggregateClass) {
        List<EventDescriptor> eventDescriptors = eventDescriptorRepository.findAllByAggregateIdSinceSnapshot(aggregateClass, aggregateId, snapshotVersion);

        return loadFromEventDescriptors(aggregateId, aggregateClass, eventDescriptors);
    }

    private <T extends AggregateRoot> Optional<T> loadFromEventDescriptors(UUID aggregateId, Class<T> aggregateClass, List<EventDescriptor> eventDescriptors) {
        Optional<T> result;
        if(!eventDescriptors.isEmpty()) {
            T aggregate = AggregateRoot.create(aggregateId, aggregateClass);

            for(EventDescriptor eventDescriptor: eventDescriptors) {
                aggregate = aggregateCommitter.apply(aggregate, eventDescriptor);
            }

            result = Optional.of(aggregate);
        } else {
            result = Optional.empty();
        }

        return result;
    }
}
