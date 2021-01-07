package com.aixoft.escassandra.component;

import com.datastax.oss.driver.api.core.cql.PreparedStatement;

/**
 * The interface Prepared statements.
 */
public interface PreparedStatements {
    /**
     * Gets insert prepared statement.
     *
     * @param aggregateDataClass Aggregate data class.
     *
     * @return Statement to insert events for given aggregate data type.
     */
    PreparedStatement getInsertPreparedStatement(Class<?> aggregateDataClass);

    /**
     * Gets select all prepared statement.
     *
     * @param aggregateDataClass Aggregate data class.
     *
     * @return Statement to select all events for given aggregate data type.
     */
    PreparedStatement getSelectAllPreparedStatement(Class<?> aggregateDataClass);

    /**
     * Gets select all since snapshot prepared statement.
     *
     * @param aggregateDataClass Aggregate data class.
     *
     * @return Statement to select all events with major version greater then provided for given aggregate data type.
     */
    PreparedStatement getSelectAllSinceSnapshotPreparedStatement(Class<?> aggregateDataClass);
}
