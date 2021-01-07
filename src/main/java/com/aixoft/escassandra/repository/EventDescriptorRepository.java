package com.aixoft.escassandra.repository;

import com.aixoft.escassandra.repository.model.EventDescriptor;
import lombok.NonNull;

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
     * @param aggregateDataClass    Aggregate data class.
     * @param aggregateId           UUID of aggregate for which EventDescriptors will be inserted.
     * @param eventDescriptors      EventDescriptors to be inserted.
     *
     * @return true if insert was successful or false otherwise.
     */
    List<EventDescriptor> insertAll(Class<?> aggregateDataClass, UUID aggregateId, List<EventDescriptor> eventDescriptors);

    /**
     * Find all event descriptors for aggregate of given data type and id.
     *
     * @param aggregateDataClass    Aggregate data class.
     * @param aggregateId           UUID of aggregate.
     *
     * @return List of event descriptors.
     */
    List<EventDescriptor> findAllByAggregateId(Class<?> aggregateDataClass, UUID aggregateId);

    /**
     * Find all event descriptors since given major version (snapshot number)
     * for aggregate of given data type and id.
     *
     * @param aggregateDataClass    Aggregate data class.
     * @param aggregateId           UUID of aggregate.
     * @param snapshotVersion       Major version of the event ({@link com.aixoft.escassandra.model.EventVersion#getMajor()})
     *                              from which aggregate will be restored.
     *
     * @return List of event descriptors.
     */

    List<EventDescriptor> findAllByAggregateIdSinceSnapshot(@NonNull Class<?> aggregateDataClass,
                                                                   @NonNull UUID aggregateId,
                                                                   int snapshotVersion);
}
