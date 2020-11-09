package com.aixoft.escassandra.service.impl;

import com.aixoft.escassandra.aggregate.AggregateRoot;
import com.aixoft.escassandra.component.AggregatePublisher;
import com.aixoft.escassandra.exception.checked.AggregateNotFoundException;
import com.aixoft.escassandra.exception.checked.AggregateFailedSaveException;
import com.aixoft.escassandra.repository.ReactiveEventDescriptorRepository;
import com.aixoft.escassandra.repository.model.EventDescriptor;
import com.aixoft.escassandra.service.ReactiveAggregateStore;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Slf4j
public class ReactiveCassandraAggregateStore implements ReactiveAggregateStore {
    ReactiveEventDescriptorRepository eventDescriptorRepository;
    AggregatePublisher aggregatePublisher;

    @Override
    public <T extends AggregateRoot> Mono<T> save(T aggregate) {

        List<EventDescriptor> newEventDescriptors = EventDescriptor.fromEvents(aggregate.getUncommittedChanges(), aggregate.getCommittedVersion());

        return eventDescriptorRepository.insertAll(aggregate.getClass(), aggregate.getId(), newEventDescriptors)
            .flatMap(inserted -> Boolean.TRUE.equals(inserted) ?
                Mono.fromCallable(() -> aggregatePublisher.applyAndPublish(aggregate, newEventDescriptors))
                : Mono.error(AggregateFailedSaveException::new));
    }

    @Override
    public <T extends AggregateRoot> Mono<T> loadById(UUID aggregateId, Class<T> aggregateClass) {

         return eventDescriptorRepository.findAllByAggregateId(aggregateClass, aggregateId)
            .switchIfEmpty(Flux.error(AggregateNotFoundException::new))
            .reduceWith(
                () -> AggregateRoot.create(aggregateId, aggregateClass),
                aggregatePublisher::apply
            );
    }

    @Override
    public <T extends AggregateRoot> Mono<T> loadById(UUID aggregateId, int baseSnapshotVersion, Class<T> aggregateClass) {
        return eventDescriptorRepository.findAllByAggregateIdSinceSnapshot(aggregateClass, aggregateId, baseSnapshotVersion)
            .switchIfEmpty(Flux.error(AggregateNotFoundException::new))
            .reduceWith(
                () -> AggregateRoot.create(aggregateId, aggregateClass),
                aggregatePublisher::apply
            );
    }
}
