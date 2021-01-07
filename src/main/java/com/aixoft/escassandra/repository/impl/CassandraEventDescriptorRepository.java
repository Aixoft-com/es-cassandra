package com.aixoft.escassandra.repository.impl;

import com.aixoft.escassandra.component.CassandraSession;
import com.aixoft.escassandra.repository.EventDescriptorRepository;
import com.aixoft.escassandra.repository.StatementBinder;
import com.aixoft.escassandra.repository.converter.EventReadingConverter;
import com.aixoft.escassandra.repository.model.EventDescriptor;
import com.aixoft.escassandra.repository.util.EventDescriptorRowUtil;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Cassandra repository to store {@link EventDescriptor}.
 */
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class CassandraEventDescriptorRepository implements EventDescriptorRepository {
    CqlSession session;
    StatementBinder statementBinder;
    EventReadingConverter eventReadingConverter;

    /**
     * Instantiates a new Cassandra event descriptor repository.
     *
     * @param cassandraSession      Cassandra session.
     * @param statementBinder       Statement binder.
     * @param eventReadingConverter Event reading converter.
     */
    public CassandraEventDescriptorRepository(CassandraSession cassandraSession, StatementBinder statementBinder, EventReadingConverter eventReadingConverter) {
        this.session = cassandraSession.getSession();
        this.statementBinder = statementBinder;
        this.eventReadingConverter = eventReadingConverter;
    }

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
     *
     * @param eventDescriptors  EventDescriptors to be inserted.
     * @return true if insert was successful or false otherwise.
     */
    @Override
    public List<EventDescriptor> insertAll(@NonNull Class<?> aggregateDataClass,
                          @NonNull UUID aggregateId,
                          @NonNull List<EventDescriptor> eventDescriptors) {
        return session.execute(statementBinder.bindInsertEventDescriptors(aggregateDataClass, aggregateId, eventDescriptors))
            .wasApplied() ? eventDescriptors : List.of();
    }

    /**
     * Find all event descriptors for aggregate of given data type and id.
     *
     * @param aggregateDataClass    Aggregate data class.
     * @param aggregateId           UUID of aggregate.
     *
     * @return List of event descriptors.
     */
    @Override
    public List<EventDescriptor> findAllByAggregateId(@NonNull Class<?> aggregateDataClass,
                                                      @NonNull UUID aggregateId) {
        return executeFindStatement(
            statementBinder.bindFindAllEventDescriptors(aggregateDataClass, aggregateId)
        );
    }

    /**
     * Find all event descriptors since given major version ({@link com.aixoft.escassandra.model.EventVersion#getMajor()})
     * for aggregate of given data type and id.
     *
     * @param aggregateDataClass    Aggregate data class.
     * @param aggregateId           UUID of aggregate.
     * @param snapshotVersion       Major version of the event ({@link com.aixoft.escassandra.model.EventVersion#getMajor()})
     *                              from which aggregate will be restored.
     *
     * @return List of event descriptors.
     */
    @Override
    public List<EventDescriptor> findAllByAggregateIdSinceSnapshot(@NonNull Class<?> aggregateDataClass,
                                                                          @NonNull UUID aggregateId,
                                                                          int snapshotVersion) {
        return executeFindStatement(
            statementBinder.bindFindAllSinceLastSnapshotEventDescriptors(aggregateDataClass, aggregateId, snapshotVersion)
        );
    }

    private List<EventDescriptor> executeFindStatement(BoundStatement statement) {
        ResultSet resultSet = session.execute(statement);

        return resultSet.all().stream()
            .map(row -> EventDescriptorRowUtil.toEventDescriptor(row, eventReadingConverter))
            .collect(Collectors.toList());
    }

}
