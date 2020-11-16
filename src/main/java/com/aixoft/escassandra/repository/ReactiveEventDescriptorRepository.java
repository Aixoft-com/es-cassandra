package com.aixoft.escassandra.repository;

import com.aixoft.escassandra.aggregate.AggregateRoot;
import com.aixoft.escassandra.repository.model.EventDescriptor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

/**
 * Reactive repository to store {@link EventDescriptor}.
 */
public interface ReactiveEventDescriptorRepository {
    /**
     * Creates {@link Mono} for inserting {@link EventDescriptor} list to database.
     * <p>
     * If list contains more then one element, then inserting will be performed in a batch.
     * <p>
     * If any event with same version is is already persisted in the database then operation will fail and no element
     * will be stored.
     *
     * @param aggregateClass   Aggregate class.
     * @param aggregateId      UUID of aggregate for which EventDescriptors will be inserted.
     * @param eventDescriptors EventDescriptors to be inserted.
     *
     * @return Mono with TRUE indicating if insert was successful or FALSE otherwise.
     */
    Mono<Boolean> insertAll(Class<? extends AggregateRoot> aggregateClass,
                            UUID aggregateId,
                            List<EventDescriptor> eventDescriptors);

    /**
     * Creates Flux for finding all event descriptors for aggregate of given type and id.
     *
     * @param aggregateClass Aggregate class.
     * @param aggregateId    UUID of aggregate.
     *
     * @return Flux from event descriptors.
     */
    Flux<EventDescriptor> findAllByAggregateId(Class<? extends AggregateRoot> aggregateClass, UUID aggregateId);

    /**
     * Creates Flux for finding all event descriptors since given major version (snapshot number)
     * for aggregate of given type and id.
     *
     * @param aggregateClass  Aggregate class.
     * @param aggregateId     UUID of aggregate.
     * @param snapshotVersion Major version of the event ({@link com.aixoft.escassandra.model.EventVersion#getMajor()}).
     *
     * @return Flux from event descriptors.
     */
    Flux<EventDescriptor> findAllByAggregateIdSinceSnapshot(Class<? extends AggregateRoot> aggregateClass,
                                                            UUID aggregateId,
                                                            int snapshotVersion);
}
