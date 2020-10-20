package com.aixoft.escassandra.repository;

import com.aixoft.escassandra.aggregate.AggregateRoot;
import com.aixoft.escassandra.repository.model.EventDescriptor;

import java.util.List;
import java.util.UUID;

public interface EventDescriptorRepository {
    boolean insertAll(Class<? extends AggregateRoot> aggregateClass, UUID aggregateId, List<EventDescriptor> newEventDescriptors);
    List<EventDescriptor> findAllByAggregateId(Class<? extends AggregateRoot> aggregateClass, UUID aggregateId);
    List<EventDescriptor> findAllByAggregateIdSinceLastSnapshot(Class<? extends AggregateRoot> aggregateClass, UUID aggregateId, int snapshotVersion);
}
