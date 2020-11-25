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

/**
 * Creates prepared statements for each aggregate type to optimize performance of cassandra queries.
 */
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class PreparedStatementsComponent implements PreparedStatements {
    private static final String INSERT_STATEMENT_FORMAT = "INSERT INTO %s (aggregateId, majorVersion, minorVersion, event) VALUES (?, ?, ?, ?) IF NOT EXISTS";
    private static final String SELECT_ALL_STATEMENT_FORMAT = "SELECT * FROM %s WHERE aggregateId = ?";
    private static final String SELECT_ALL_SINCE_MAJOR_VERSION_STATEMENT_FORMAT = "SELECT * FROM %s WHERE aggregateId = ? and majorVersion >= ?";

    Map<String, PreparedStatement> insertStatementsByAggregateClass = new HashMap<>();
    Map<String, PreparedStatement> selectAllStatementsByAggregateClass = new HashMap<>();
    Map<String, PreparedStatement> selectAllSinceSnapshotStatementsByAggregateClass = new HashMap<>();

    /**
     * Instantiates a new Prepared statements component.
     *
     * @param aggregateComponent the aggregate component
     * @param cassandraSession   the cassandra session
     */
    public PreparedStatementsComponent(@NonNull AggregateComponent aggregateComponent, @NonNull CassandraSession cassandraSession) {
        if (aggregateComponent.getClasses() != null) {
            initPreparedStatements(aggregateComponent.getClasses(), cassandraSession.getSession());
        }
    }


    /**
     * Gets insert prepared statement.
     *
     * @return Statement to insert events for given aggregate type.
     */
    @Override
    public PreparedStatement getInsertPreparedStatement(@NonNull Class<? extends AggregateRoot> aggregateClass) {
        return insertStatementsByAggregateClass.get(aggregateClass.getName());
    }

    /**
     * Gets select all prepared statement.
     *
     * @param aggregateClass Aggregate class.
     * @return Statement to select all events for given aggregate type.
     */
    @Override
    public PreparedStatement getSelectAllPreparedStatement(@NonNull Class<? extends AggregateRoot> aggregateClass) {
        return selectAllStatementsByAggregateClass.get(aggregateClass.getName());
    }

    /**
     * Gets select all since snapshot prepared statement.
     *
     * @param aggregateClass Aggregate class.
     *
     * @return Statement to select all events with major version greater then provided for given aggregate type.
     */
    @Override
    public PreparedStatement getSelectAllSinceSnapshotPreparedStatement(@NonNull Class<? extends AggregateRoot> aggregateClass) {
        return selectAllSinceSnapshotStatementsByAggregateClass.get(aggregateClass.getName());
    }

    private void initPreparedStatements(List<Class<? extends AggregateRoot>> aggregateClasses, @NonNull CqlSession session) {
        aggregateClasses.forEach(aggregateClass -> {
            String tableName = TableNameUtil.fromAggregateClass(aggregateClass);

            insertStatementsByAggregateClass.put(aggregateClass.getName(), session.prepare(SimpleStatement.newInstance(String.format(INSERT_STATEMENT_FORMAT, tableName))));
            selectAllStatementsByAggregateClass.put(aggregateClass.getName(), session.prepare(SimpleStatement.newInstance(String.format(SELECT_ALL_STATEMENT_FORMAT, tableName))));
            selectAllSinceSnapshotStatementsByAggregateClass.put(aggregateClass.getName(), session.prepare(SimpleStatement.newInstance(String.format(SELECT_ALL_SINCE_MAJOR_VERSION_STATEMENT_FORMAT, tableName))));
        });
    }
}
