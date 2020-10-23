package com.aixoft.escassandra.repository.impl;

import com.aixoft.escassandra.aggregate.AggregateRoot;
import com.aixoft.escassandra.component.PreparedStatements;
import com.aixoft.escassandra.repository.StatementBinder;
import com.aixoft.escassandra.repository.converter.EventWritingConverter;
import com.aixoft.escassandra.repository.model.EventDescriptor;
import com.datastax.oss.driver.api.core.cql.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class StatementBinderComponent implements StatementBinder {
    PreparedStatements preparedStatements;
    EventWritingConverter eventWritingConverter;

    @Override
    public Statement bindInsertEventDescriptors(@NonNull Class<? extends AggregateRoot> aggregateClass, @NonNull UUID aggregateId, @NonNull List<EventDescriptor> eventDescriptors) {
        PreparedStatement preparedStatement = preparedStatements.getInsertPreparedStatement(aggregateClass);

        Statement<? extends Statement>  insertStatement;
        if(eventDescriptors.size() == 1) {
            insertStatement = bindInsertStatement(preparedStatement, aggregateId, eventDescriptors.get(0));
        }
        else {
            insertStatement = BatchStatement.newInstance(
                BatchType.LOGGED,
                eventDescriptors.stream().map(eventDescriptor -> bindInsertStatement(preparedStatement, aggregateId, eventDescriptor))
                    .collect(Collectors.toList())
            );
        }

        return insertStatement;
    }

    private BoundStatement bindInsertStatement(PreparedStatement preparedStatement, UUID aggregateId, EventDescriptor eventDescriptor) {
        return preparedStatement.bind(
            aggregateId,
            eventDescriptor.getEventVersion().getMajor(),
            eventDescriptor.getEventVersion().getMinor(),
            eventWritingConverter.convert(eventDescriptor.getEvent()));
    }

    @Override
    public BoundStatement  bindFindAllEventDescriptors(@NonNull Class<? extends AggregateRoot> aggregateClass, @NonNull UUID aggregateId) {
        PreparedStatement preparedStatement = preparedStatements.getSelectAllPreparedStatement(aggregateClass);

        return preparedStatement.bind(aggregateId);
    }

    @Override
    public BoundStatement bindFindAllSinceLastSnapshotEventDescriptors(@NonNull Class<? extends AggregateRoot> aggregateClass, @NonNull UUID aggregateId, int snapshotVersion) {
        PreparedStatement preparedStatement = preparedStatements.getSelectAllSinceSnapshotPreparedStatement(aggregateClass);

        return preparedStatement.bind(aggregateId, snapshotVersion);
    }
}
