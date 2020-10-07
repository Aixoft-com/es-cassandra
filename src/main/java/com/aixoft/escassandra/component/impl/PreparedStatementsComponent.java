package com.aixoft.escassandra.component.impl;

import com.aixoft.escassandra.aggregate.AggregateRoot;
import com.aixoft.escassandra.annotation.Aggregate;
import com.aixoft.escassandra.component.CassandraSession;
import com.aixoft.escassandra.component.PreparedStatements;
import com.aixoft.escassandra.component.registrar.AggregateComponent;
import com.aixoft.escassandra.exception.runtime.AggregateAnnotationMissingException;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Session;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class PreparedStatementsComponent implements PreparedStatements {
    private final static String INSERT_STATEMENT_FORMAT = "INSERT INTO %s (aggregateId, majorVersion, minorVersion, eventId, event) VALUES (?, ?, ?, ?, ?)";
    private final static String SELECT_ALL_STATEMENT_FORMAT = "SELECT * FROM %s WHERE aggregateId = ?";
    private final static String SELECT_ALL_SINCE_MAJOR_VERSION_STATEMENT_FORMAT = "SELECT * FROM %s WHERE aggregateId = ? and majorVersion >= ?";

    Map<Class<? extends AggregateRoot>, PreparedStatement> insertStatementsByAggregateClass = new HashMap<>();
    Map<Class<? extends AggregateRoot>, PreparedStatement> selectAllStatementsByAggregateClass = new HashMap<>();
    Map<Class<? extends AggregateRoot>, PreparedStatement> selectAllSinceSnapshotStatementsByAggregateClass = new HashMap<>();

    public PreparedStatementsComponent(@NonNull AggregateComponent aggregateComponent, @NonNull CassandraSession cassandraSession) {
        if (aggregateComponent.getClasses() != null) {
            initPreparedStatements(aggregateComponent.getClasses(), cassandraSession.getSession());
        }
    }

    @Override
    public PreparedStatement getInsertPreparedStatement(@NonNull Class<? extends AggregateRoot> aggregateClass) {
        return insertStatementsByAggregateClass.get(aggregateClass);
    }

    @Override
    public PreparedStatement getSelectAllPreparedStatement(@NonNull Class<? extends AggregateRoot> aggregateClass) {
        return selectAllStatementsByAggregateClass.get(aggregateClass);
    }

    @Override
    public PreparedStatement getSelectAllSinceSnapshotPreparedStatement(@NonNull Class<? extends AggregateRoot> aggregateClass) {
        return selectAllSinceSnapshotStatementsByAggregateClass.get(aggregateClass);
    }

    private void initPreparedStatements(List<Class> aggregateClasses, @NonNull Session session) {
        aggregateClasses.forEach(aggregateClass -> {
            String partitionKey = getPartitionKey(aggregateClass);

            insertStatementsByAggregateClass.put(aggregateClass, session.prepare(String.format(INSERT_STATEMENT_FORMAT, partitionKey)));
            selectAllStatementsByAggregateClass.put(aggregateClass, session.prepare(String.format(SELECT_ALL_STATEMENT_FORMAT, partitionKey)));
            selectAllSinceSnapshotStatementsByAggregateClass.put(aggregateClass, session.prepare(String.format(SELECT_ALL_SINCE_MAJOR_VERSION_STATEMENT_FORMAT, partitionKey)));
        });
    }

    private static String getPartitionKey(Class<? extends AggregateRoot> aggregateClass) {
        Aggregate annotation = aggregateClass.getAnnotation(Aggregate.class);

        String tableName;
        if (annotation != null) {
            tableName = annotation.partitionKey();
        } else {
            throw new AggregateAnnotationMissingException(String.format("%s not annotated with %s", aggregateClass.getName(), Aggregate.class.getName()));
        }

        return tableName;
    }
}
