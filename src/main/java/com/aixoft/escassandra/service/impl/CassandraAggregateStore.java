package com.aixoft.escassandra.service.impl;

import com.aixoft.escassandra.aggregate.Aggregate;
import com.aixoft.escassandra.aggregate.AggregateRoot;
import com.aixoft.escassandra.repository.EventDescriptorRepository;
import com.aixoft.escassandra.repository.model.EventDescriptor;
import com.aixoft.escassandra.service.AggregateStore;
import com.aixoft.escassandra.service.EventRouter;
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
    EventRouter eventRouter;

    /**
     * Persists all uncommitted events to the database.
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
     * @param aggregate     Aggregate to be stored.
     * @param <T>           Aggregate data type.
     * @return              Committed copy of aggregate if operation was successful or empty otherwise.
     */
    @Override
    public <T> Optional<Aggregate<T>> save(Aggregate<T> aggregate) {

        Aggregate<T> newAggregate = aggregate.committedCopy();

        List<EventDescriptor> eventDescriptors = aggregate.getUncommittedEvents();
        List<EventDescriptor> savedEventDescriptions = eventDescriptorRepository.insertAll(newAggregate.getData().getClass(), newAggregate.getId(), eventDescriptors);

        Optional<Aggregate<T>> result;
        if(savedEventDescriptions.isEmpty()) {
            result = Optional.empty();
        } else {
            savedEventDescriptions.forEach(eventDescriptor -> eventRouter.publish(
                eventDescriptor.getEvent(),
                eventDescriptor.getEventVersion(),
                newAggregate.getId()));

            result = Optional.of(newAggregate);
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
     * Committed and current version of the aggregate will be equal last event version (See {@link AggregateRoot#getCommittedVersion()}).
     *
     * @param aggregateId           UUID of the aggregate.
     * @param aggregateDataClass    Aggregate data class.
     * @param <T>                   Aggregate data type.
     *
     * @return Restored aggregate or empty if aggregate not found.
     */
    @Override
    public <T> Optional<Aggregate<T>> loadById(UUID aggregateId, Class<T> aggregateDataClass) {
        List<EventDescriptor> eventDescriptors = eventDescriptorRepository.findAllByAggregateId(aggregateDataClass, aggregateId);

        return loadFromEventDescriptors(aggregateId, eventDescriptors);
    }

    /**
     * Restores aggregate from events using event sourcing.
     * <p>
     * All events stored in database since given {@code snapshotVersion} are used to restore aggregate.
     * <p>
     * Effective if snapshots are being used.
     * <p>
     * Committed and current version of the aggregate will be equal last event version (See {@link AggregateRoot#getCommittedVersion()}).
     *
     * @param aggregateId        UUID of the aggregate.
     * @param snapshotVersion    Major version of the event ({@link com.aixoft.escassandra.model.EventVersion}#getMajor()).
     * @param aggregateDataClass Aggregate data class.
     * @param <T>                Aggregate data type.
     * @return Restored aggregate or empty if aggregate not found.
     */
    @Override
    public <T> Optional<Aggregate<T>> loadById(UUID aggregateId, int snapshotVersion, Class<T> aggregateDataClass) {
        List<EventDescriptor> eventDescriptors = eventDescriptorRepository.findAllByAggregateIdSinceSnapshot(aggregateDataClass, aggregateId, snapshotVersion);

        return loadFromEventDescriptors(aggregateId, eventDescriptors);
    }

    private <T> Optional<Aggregate<T>> loadFromEventDescriptors(UUID aggregateId, List<EventDescriptor> eventDescriptors) {
        Optional<Aggregate<T>> result;
        if(!eventDescriptors.isEmpty()) {
            result = Optional.of(Aggregate.restoreFromEvents(aggregateId, eventDescriptors));
        } else {
            result = Optional.empty();
        }

        return result;
    }
}
