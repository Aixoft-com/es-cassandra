package com.aixoft.escassandra.service.impl;

import com.aixoft.escassandra.aggregate.AggregateRoot;
import com.aixoft.escassandra.component.AggregateSubscribedMethods;
import com.aixoft.escassandra.exception.runtime.AggregateCreationException;
import com.aixoft.escassandra.model.Event;
import com.aixoft.escassandra.model.EventVersion;
import com.aixoft.escassandra.model.SnapshotEvent;
import com.aixoft.escassandra.repository.ReactiveEventDescriptorRepository;
import com.aixoft.escassandra.repository.model.EventDescriptor;
import com.aixoft.escassandra.service.EventRouter;
import com.aixoft.escassandra.service.ReactiveAggregateStore;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Slf4j
public class ReactiveCassandraAggregateStore implements ReactiveAggregateStore {
    ReactiveEventDescriptorRepository eventDescriptorRepository;
    AggregateSubscribedMethods aggregateSubscribedMethods;
    EventRouter eventRouter;

    @Override
    public Mono<EventVersion> save(AggregateRoot aggregate) {
        List<EventDescriptor> newEventDescriptors = new ArrayList<>(aggregate.getUncommittedChanges().size());

        EventVersion currentEventVersion = aggregate.getCommittedVersion();
        if (currentEventVersion == null) {
            currentEventVersion = EventVersion.initial();
        }

        for (Event event : aggregate.getUncommittedChanges()) {
            currentEventVersion = currentEventVersion.getNext(event instanceof SnapshotEvent);

            newEventDescriptors.add(new EventDescriptor(
                currentEventVersion,
                event)
            );
        }

        EventVersion lastEvent = currentEventVersion;

        return eventDescriptorRepository.insertAll(aggregate.getClass(), aggregate.getId(), newEventDescriptors)
            .doOnNext(res -> {
                    if(res == true) {
                        //TODO: Aggregate shall be cloned here
                        aggregate.setCommittedVersion(lastEvent);

                        for (EventDescriptor eventDescriptor : newEventDescriptors) {
                            Event event = eventDescriptor.getEvent();

                            //TODO: published aggregate shall be immutable
                            apply(event, aggregate);
                            eventRouter.publish(event, aggregate);
                        }

                        aggregate.markChangesAsCommitted();
                    }
                }
            )
            .flatMap(inserted -> inserted ? Mono.just(lastEvent) : Mono.empty());
    }

    @Override
    public <T extends AggregateRoot> Mono<T> findById(UUID aggregateId, Class<T> aggregateClass) {

         return eventDescriptorRepository.findAllByAggregateId(aggregateClass, aggregateId)
            .reduceWith(
                () -> createAggregate(aggregateId, aggregateClass),
                this::loadFromHistory
            );
    }

    @Override
    public <T extends AggregateRoot> Mono<T> findById(UUID aggregateId, int baseSnapshotVersion, Class<T> aggregateClass) {
        return eventDescriptorRepository.findAllByAggregateIdSinceLastSnapshot(aggregateClass, aggregateId, baseSnapshotVersion)
            .reduceWith(
                () -> createAggregate(aggregateId, aggregateClass),
                this::loadFromHistory
            );
    }

    private <T extends AggregateRoot> T loadFromHistory(T aggregateRoot, EventDescriptor eventDescriptor) {
        apply(eventDescriptor.getEvent(), aggregateRoot);

        aggregateRoot.setCommittedVersion(eventDescriptor.getEventVersion());

        return aggregateRoot;
    }

    private void apply(Event event, AggregateRoot aggregateRoot) {
        aggregateSubscribedMethods.invokeAggregateMethodForEvent(aggregateRoot, event);
    }

    private <T extends AggregateRoot> T createAggregate(UUID aggregateId, Class<T> aggregateClass) {
        T aggregateRoot;
        try {
            aggregateRoot = aggregateClass.getDeclaredConstructor(UUID.class).newInstance(aggregateId);
        } catch (ReflectiveOperationException ex) {
            log.error(ex.getMessage(), ex);
            throw new AggregateCreationException(String.format("Not able to create instance of '%s'", aggregateClass.getName()));
        }
        return aggregateRoot;
    }

}
