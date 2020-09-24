package com.aixoft.escassandra.component;

import com.aixoft.escassandra.aggregate.AggregateRoot;
import com.datastax.driver.core.PreparedStatement;

public interface PreparedStatements {
    PreparedStatement getInsertPreparedStatement(Class<? extends AggregateRoot> aggregateClass);

    PreparedStatement getSelectAllPreparedStatement(Class<? extends AggregateRoot> aggregateClass);

    PreparedStatement getSelectAllSinceSnapshotPreparedStatement(Class<? extends AggregateRoot> aggregateClass);
}
