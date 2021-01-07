package com.aixoft.escassandra.repository.impl;

import com.aixoft.escassandra.component.PreparedStatements;
import com.aixoft.escassandra.exception.runtime.AggregateStatementNotFoundException;
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

/**
 * Binds prepared statements with parameters
 */
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class StatementBinderComponent implements StatementBinder {
    PreparedStatements preparedStatements;
    EventWritingConverter eventWritingConverter;

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
    @Override
    public Statement bindInsertEventDescriptors(@NonNull Class<?> aggregateDataClass, @NonNull UUID aggregateId, @NonNull List<EventDescriptor> eventDescriptors) {
        PreparedStatement preparedStatement = preparedStatements.getInsertPreparedStatement(aggregateDataClass);

        if(preparedStatement == null) {
            throw new AggregateStatementNotFoundException(String.format("Statement not found for %s, it might not be included in scanned package", aggregateDataClass));
        }

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

    /**
     * Bind statement to select all events for given aggregate data.
     *
     * @param aggregateDataClass    Aggregate data class.
     * @param aggregateId           UUID of aggregate.
     *
     * @return Bound statement.
     */
    @Override
    public BoundStatement  bindFindAllEventDescriptors(@NonNull Class<?> aggregateDataClass, @NonNull UUID aggregateId) {
        PreparedStatement preparedStatement = preparedStatements.getSelectAllPreparedStatement(aggregateDataClass);

        return preparedStatement.bind(aggregateId);
    }

    /**
     * Bind statement to select all events with major version greater then provided for given aggregate type.
     *
     * @param aggregateDataClass    Aggregate data class.
     * @param aggregateId           UUID of aggregate.
     * @param snapshotVersion       Major version of the event ({@link com.aixoft.escassandra.model.EventVersion#getMajor()}).
     *
     * @return Bound statement.
     */
    @Override
    public BoundStatement bindFindAllSinceLastSnapshotEventDescriptors(@NonNull Class<?> aggregateDataClass, @NonNull UUID aggregateId, int snapshotVersion) {
        PreparedStatement preparedStatement = preparedStatements.getSelectAllSinceSnapshotPreparedStatement(aggregateDataClass);

        return preparedStatement.bind(aggregateId, snapshotVersion);
    }
}
