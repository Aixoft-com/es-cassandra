package com.aixoft.escassandra.component.impl;

import com.aixoft.escassandra.aggregate.AggregateRoot;
import com.aixoft.escassandra.component.CassandraSession;
import com.aixoft.escassandra.component.PreparedStatements;
import com.aixoft.escassandra.component.registrar.AggregateComponent;
import com.aixoft.escassandra.component.util.TableNameUtil;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class PreparedStatementsComponent implements PreparedStatements {
    private final static String INSERT_STATEMENT_FORMAT = "INSERT INTO %s (aggregateId, majorVersion, minorVersion, event) VALUES (?, ?, ?, ?) IF NOT EXISTS";
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

    private void initPreparedStatements(List<Class> aggregateClasses, @NonNull CqlSession session) {
        aggregateClasses.forEach(aggregateClass -> {
            String tableName = TableNameUtil.fromAggregateClass(aggregateClass);

            insertStatementsByAggregateClass.put(aggregateClass, session.prepare(SimpleStatement.newInstance(String.format(INSERT_STATEMENT_FORMAT, tableName))));
            selectAllStatementsByAggregateClass.put(aggregateClass, session.prepare(SimpleStatement.newInstance(String.format(SELECT_ALL_STATEMENT_FORMAT, tableName))));
            selectAllSinceSnapshotStatementsByAggregateClass.put(aggregateClass, session.prepare(SimpleStatement.newInstance(String.format(SELECT_ALL_SINCE_MAJOR_VERSION_STATEMENT_FORMAT, tableName))));
        });
    }
}
