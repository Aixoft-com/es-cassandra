package com.aixoft.escassandra.repository.impl;

import com.aixoft.escassandra.aggregate.AggregateRoot;
import com.aixoft.escassandra.component.CassandraSession;
import com.aixoft.escassandra.component.PreparedStatements;
import com.aixoft.escassandra.repository.EventDescriptorRepository;
import com.aixoft.escassandra.repository.converter.EventReadingConverter;
import com.aixoft.escassandra.repository.converter.EventWritingConverter;
import com.aixoft.escassandra.repository.model.EventDescriptor;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class CassandraEventDescriptorRepository implements EventDescriptorRepository {
    CqlSession session;
    PreparedStatements preparedStatements;
    EventWritingConverter eventWritingConverter;
    EventReadingConverter eventReadingConverter;

    public CassandraEventDescriptorRepository(@NonNull CassandraSession cassandraSession,
                                              @NonNull PreparedStatements preparedStatements,
                                              @NonNull EventWritingConverter eventWritingConverter,
                                              @NonNull EventReadingConverter eventReadingConverter) {
        this.session = cassandraSession.getSession();
        this.preparedStatements = preparedStatements;
        this.eventWritingConverter = eventWritingConverter;
        this.eventReadingConverter = eventReadingConverter;
    }

    @Override
    public void insertAll(@NonNull Class<? extends AggregateRoot> aggregateClass,
                          @NonNull UUID aggregateId,
                          @NonNull List<EventDescriptor> newEventDescriptors) {
        PreparedStatement preparedStatement = preparedStatements.getInsertPreparedStatement(aggregateClass);

        newEventDescriptors.forEach(eventDescriptor -> {
            session.execute(preparedStatement.bind(
                aggregateId,
                eventDescriptor.getMajorVersion(),
                eventDescriptor.getMinorVersion(),
                eventDescriptor.getEventId(),
                eventWritingConverter.convert(eventDescriptor.getEvent()))
            );
        });
    }

    @Override
    public List<EventDescriptor> findAllByAggregateId(@NonNull Class<? extends AggregateRoot> aggregateClass,
                                                      @NonNull UUID aggregateId) {
        PreparedStatement preparedStatement = preparedStatements.getSelectAllPreparedStatement(aggregateClass);

        ResultSet resultSet = session.execute(preparedStatement.bind(aggregateId));

        return resultSet.all().stream()
            .map(this::getEventDescriptor)
            .collect(Collectors.toList());
    }

    @Override
    public List<EventDescriptor> findAllByAggregateIdSinceLastSnapshot(@NonNull Class<? extends AggregateRoot> aggregateClass,
                                                                       @NonNull UUID aggregateId,
                                                                       int snapshotVersion) {
        PreparedStatement preparedStatement = preparedStatements.getSelectAllSinceSnapshotPreparedStatement(aggregateClass);

        ResultSet resultSet = session.execute(preparedStatement.bind(aggregateId, snapshotVersion));

        return resultSet.all().stream()
            .map(this::getEventDescriptor)
            .collect(Collectors.toList());
    }

    private EventDescriptor getEventDescriptor(Row row) {
        return new EventDescriptor(
            row.getInt(1),
            row.getInt(2),
            row.getUuid(3),
            eventReadingConverter.convert(row.getString(4))
        );
    }

}
