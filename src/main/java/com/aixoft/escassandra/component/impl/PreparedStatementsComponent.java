package com.aixoft.escassandra.component.impl;

import com.aixoft.escassandra.component.CassandraSession;
import com.aixoft.escassandra.component.PreparedStatements;
import com.aixoft.escassandra.component.registrar.AggregateDataComponent;
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
 * Creates prepared statements for each aggregate data type to optimize performance of cassandra queries.
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
     * @param aggregateDataComponent    the aggregate component
     * @param cassandraSession          the cassandra session
     */
    public PreparedStatementsComponent(@NonNull AggregateDataComponent aggregateDataComponent, @NonNull CassandraSession cassandraSession) {
        if (aggregateDataComponent.getClasses() != null) {
            initPreparedStatements(aggregateDataComponent.getClasses(), cassandraSession.getSession());
        }
    }


    /**
     * Gets insert prepared statement.
     *
     * @param aggregateDataClass Aggregate data class.
     *
     * @return Statement to insert events for given aggregate data type.
     */
    @Override
    public PreparedStatement getInsertPreparedStatement(@NonNull Class<?> aggregateDataClass) {
        return insertStatementsByAggregateClass.get(aggregateDataClass.getName());
    }

    /**
     * Gets select all prepared statement.
     *
     * @param aggregateDataClass Aggregate data class.
     * @return Statement to select all events for given aggregate data type.
     */
    @Override
    public PreparedStatement getSelectAllPreparedStatement(@NonNull Class<?> aggregateDataClass) {
        return selectAllStatementsByAggregateClass.get(aggregateDataClass.getName());
    }

    /**
     * Gets select all since snapshot prepared statement.
     *
     * @param aggregateDataClass Aggregate data class.
     *
     * @return Statement to select all events with major version greater then provided for given aggregate data type.
     */
    @Override
    public PreparedStatement getSelectAllSinceSnapshotPreparedStatement(@NonNull Class<?> aggregateDataClass) {
        return selectAllSinceSnapshotStatementsByAggregateClass.get(aggregateDataClass.getName());
    }

    private void initPreparedStatements(List<Class<?>> aggregateDataClasses, @NonNull CqlSession session) {
        aggregateDataClasses.forEach(aggregateClass -> {
            String tableName = TableNameUtil.fromAggregateDataClass(aggregateClass);

            insertStatementsByAggregateClass.put(aggregateClass.getName(), session.prepare(SimpleStatement.newInstance(String.format(INSERT_STATEMENT_FORMAT, tableName))));
            selectAllStatementsByAggregateClass.put(aggregateClass.getName(), session.prepare(SimpleStatement.newInstance(String.format(SELECT_ALL_STATEMENT_FORMAT, tableName))));
            selectAllSinceSnapshotStatementsByAggregateClass.put(aggregateClass.getName(), session.prepare(SimpleStatement.newInstance(String.format(SELECT_ALL_SINCE_MAJOR_VERSION_STATEMENT_FORMAT, tableName))));
        });
    }
}
