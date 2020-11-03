package com.aixoft.escassandra.repository;

import com.aixoft.escassandra.aggregate.AggregateRoot;
import com.aixoft.escassandra.repository.model.EventDescriptor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

public interface ReactiveEventDescriptorRepository {
    Mono<Boolean> insertAll(Class<? extends AggregateRoot> aggregateClass,
                            UUID aggregateId,
                            List<EventDescriptor> newEventDescriptors);

    Flux<EventDescriptor> findAllByAggregateId(Class<? extends AggregateRoot> aggregateClass, UUID aggregateId);
    Flux<EventDescriptor> findAllByAggregateIdSinceSnapshot(Class<? extends AggregateRoot> aggregateClass,
                                                            UUID aggregateId,
                                                            int snapshotVersion);
}
