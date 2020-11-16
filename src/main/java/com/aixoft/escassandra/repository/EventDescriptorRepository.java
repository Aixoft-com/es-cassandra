package com.aixoft.escassandra.repository;

import com.aixoft.escassandra.aggregate.AggregateRoot;
import com.aixoft.escassandra.repository.model.EventDescriptor;

import java.util.List;
import java.util.UUID;

/**
 * Repository to store {@link EventDescriptor}.
 */
public interface EventDescriptorRepository {
    /**
     * Executes insert of {@link EventDescriptor} list to database.
     * <p>
     * If list contains more then one element, then insert will be performed in a batch.
     * <p>
     * If any event with same version is is already persisted in the database then operation will fail and no element
     * will be stored.
     *
     * @param aggregateClass   Aggregate class.
     * @param aggregateId      UUID of aggregate for which EventDescriptors will be inserted.
     * @param eventDescriptors EventDescriptors to be inserted.
     *
     * @return true if insert was successful or false otherwise.
     */
    boolean insertAll(Class<? extends AggregateRoot> aggregateClass, UUID aggregateId, List<EventDescriptor> eventDescriptors);

    /**
     * Find all event descriptors for aggregate of given type and id.
     *
     * @param aggregateClass Aggregate class.
     * @param aggregateId    UUID of aggregate.
     *
     * @return List of event descriptors.
     */
    List<EventDescriptor> findAllByAggregateId(Class<? extends AggregateRoot> aggregateClass, UUID aggregateId);

    /**
     * Find all event descriptors since given major version (snapshot number)
     * for aggregate of given type and id.
     *
     * @param aggregateClass  Aggregate class.
     * @param aggregateId     UUID of aggregate.
     * @param snapshotVersion Major version of the event ({@link com.aixoft.escassandra.model.EventVersion#getMajor()}).
     *
     * @return List of event descriptors.
     */
    List<EventDescriptor> findAllByAggregateIdSinceSnapshot(Class<? extends AggregateRoot> aggregateClass, UUID aggregateId, int snapshotVersion);
}
