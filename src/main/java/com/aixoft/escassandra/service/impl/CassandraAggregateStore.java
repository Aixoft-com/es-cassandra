package com.aixoft.escassandra.service.impl;

import com.aixoft.escassandra.aggregate.AggregateRoot;
import com.aixoft.escassandra.component.AggregatePublisher;
import com.aixoft.escassandra.repository.EventDescriptorRepository;
import com.aixoft.escassandra.repository.model.EventDescriptor;
import com.aixoft.escassandra.service.AggregateStore;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Slf4j
public class CassandraAggregateStore implements AggregateStore {
    EventDescriptorRepository eventDescriptorRepository;
    AggregatePublisher aggregatePublisher;

    @Override
    public <T extends AggregateRoot> Optional<T> save(T aggregate) {
        List<EventDescriptor> newEventDescriptors = EventDescriptor.fromEvents(aggregate.getUncommittedChanges(), aggregate.getCommittedVersion());

        boolean insertSucceed = eventDescriptorRepository.insertAll(aggregate.getClass(), aggregate.getId(), newEventDescriptors);

        Optional<T> result;
        if(insertSucceed) {
            result = Optional.of(aggregatePublisher.applyAndPublish(aggregate, newEventDescriptors));
        } else {
            result = Optional.empty();
        }

        return result;
    }

    @Override
    public <T extends AggregateRoot> Optional<T> loadById(UUID aggregateId, Class<T> aggregateClass) {
        List<EventDescriptor> eventDescriptors = eventDescriptorRepository.findAllByAggregateId(aggregateClass, aggregateId);

        return loadFromEventDescriptors(aggregateId, aggregateClass, eventDescriptors);
    }

    @Override
    public <T extends AggregateRoot> Optional<T> loadById(UUID aggregateId, int baseSnapshotVersion, Class<T> aggregateClass) {
        List<EventDescriptor> eventDescriptors = eventDescriptorRepository.findAllByAggregateIdSinceSnapshot(aggregateClass, aggregateId, baseSnapshotVersion);

        return loadFromEventDescriptors(aggregateId, aggregateClass, eventDescriptors);
    }

    private <T extends AggregateRoot> Optional<T> loadFromEventDescriptors(UUID aggregateId, Class<T> aggregateClass, List<EventDescriptor> eventDescriptors) {
        Optional<T> result;
        if(!eventDescriptors.isEmpty()) {
            T aggregate = AggregateRoot.create(aggregateId, aggregateClass);

            for(EventDescriptor eventDescriptor: eventDescriptors) {
                aggregate = aggregatePublisher.apply(aggregate, eventDescriptor);
            }

            result = Optional.of(aggregate);
        } else {
            result = Optional.empty();
        }

        return result;
    }
}
