package com.aixoft.escassandra.component;

import com.aixoft.escassandra.aggregate.AggregateRoot;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;

/**
 * The interface Prepared statements.
 */
public interface PreparedStatements {
    /**
     * Gets insert prepared statement.
     *
     * @param aggregateClass Aggregate class.
     *
     * @return Statement to insert events for given aggregate type.
     */
    PreparedStatement getInsertPreparedStatement(Class<? extends AggregateRoot> aggregateClass);

    /**
     * Gets select all prepared statement.
     *
     * @param aggregateClass Aggregate class.
     *
     * @return Statement to select all events for given aggregate type.
     */
    PreparedStatement getSelectAllPreparedStatement(Class<? extends AggregateRoot> aggregateClass);

    /**
     * Gets select all since snapshot prepared statement.
     *
     * @param aggregateClass Aggregate class.
     *
     * @return Statement to select all events with major version greater then provided for given aggregate type.
     */
    PreparedStatement getSelectAllSinceSnapshotPreparedStatement(Class<? extends AggregateRoot> aggregateClass);
}
