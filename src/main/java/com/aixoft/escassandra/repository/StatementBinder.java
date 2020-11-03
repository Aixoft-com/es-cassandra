package com.aixoft.escassandra.repository;

import com.aixoft.escassandra.aggregate.AggregateRoot;
import com.aixoft.escassandra.repository.model.EventDescriptor;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.Statement;

import java.util.List;
import java.util.UUID;

public interface StatementBinder {

    Statement bindInsertEventDescriptors(Class<? extends AggregateRoot> aggregateClass, UUID aggregateId, List<EventDescriptor> eventDescriptors);

    BoundStatement bindFindAllEventDescriptors(Class<? extends AggregateRoot> aggregateClass, UUID aggregateId);

    BoundStatement bindFindAllSinceLastSnapshotEventDescriptors(Class<? extends AggregateRoot> aggregateClass, UUID aggregateId, int snapshotVersion);
}
