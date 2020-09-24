package com.aixoft.escassandra.service;

import com.aixoft.escassandra.aggregate.AggregateRoot;

import java.util.UUID;

public interface AggregateStore {
    void save(AggregateRoot aggregate);

    <T extends AggregateRoot> T findById(UUID aggregateId, Class<T> aggregateClass);

    <T extends AggregateRoot> T findById(UUID aggregateId, int baseSnapshotVersion, Class<T> aggregateClass);
}
