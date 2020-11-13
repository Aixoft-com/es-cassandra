package com.aixoft.escassandra.service;

import com.aixoft.escassandra.aggregate.AggregateRoot;

import java.util.Optional;
import java.util.UUID;

public interface AggregateStore {
    <T extends AggregateRoot> Optional<T> save(T aggregate);

    <T extends AggregateRoot> Optional<T> loadById(UUID aggregateId, Class<T> aggregateClass);

    <T extends AggregateRoot> Optional<T> loadById(UUID aggregateId, int baseSnapshotVersion, Class<T> aggregateClass);
}
