package com.aixoft.escassandra.service;

import com.aixoft.escassandra.aggregate.AggregateRoot;
import com.aixoft.escassandra.model.EventVersion;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ReactiveAggregateStore {
    Mono<EventVersion> save(AggregateRoot aggregate);

    <T extends AggregateRoot> Mono<T> loadById(UUID aggregateId, Class<T> aggregateClass);

    <T extends AggregateRoot> Mono<T> loadById(UUID aggregateId, int baseSnapshotVersion, Class<T> aggregateClass);
}
