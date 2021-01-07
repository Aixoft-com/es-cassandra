package com.aixoft.escassandra.repository;

import com.aixoft.escassandra.repository.model.EventDescriptor;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.Statement;

import java.util.List;
import java.util.UUID;

/**
 * The interface Statement binder.
 */
public interface StatementBinder {

    /**
     * Bind insert statement with aggregate and event descriptors.
     * <p>
     * If number of events is greater then one then batch statement will be created.
     *
     * @param aggregateDataClass    Aggregate data class.
     * @param aggregateId           UUID of aggregate for which EventDescriptors will be inserted.
     * @param eventDescriptors      EventDescriptors to be inserted.
     *
     * @return Bound statement.
     */
    Statement bindInsertEventDescriptors(Class<?> aggregateDataClass, UUID aggregateId, List<EventDescriptor> eventDescriptors);

    /**
     * Bind statement to select all events for given aggregate.
     *
     * @param aggregateDataClass    Aggregate data class.
     * @param aggregateId           UUID of aggregate.
     *
     * @return Bound statement.
     */
    BoundStatement bindFindAllEventDescriptors(Class<?> aggregateDataClass, UUID aggregateId);

    /**
     * Bind statement to select all events with major version greater then provided for given aggregate type.
     *
     * @param aggregateClass  Aggregate data class.
     * @param aggregateId     UUID of aggregate.
     * @param snapshotVersion Major version of the event ({@link com.aixoft.escassandra.model.EventVersion#getMajor()}).
     *
     * @return Bound statement.
     */
    BoundStatement bindFindAllSinceLastSnapshotEventDescriptors(Class<?> aggregateClass, UUID aggregateId, int snapshotVersion);
}
