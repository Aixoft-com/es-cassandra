package com.aixoft.escassandra.service.impl;

import com.aixoft.escassandra.aggregate.AggregateRoot;
import com.aixoft.escassandra.component.AggregateSubscribedMethods;
import com.aixoft.escassandra.exception.runtime.AggregateCreationException;
import com.aixoft.escassandra.model.Event;
import com.aixoft.escassandra.model.EventVersion;
import com.aixoft.escassandra.model.SnapshotEvent;
import com.aixoft.escassandra.repository.EventDescriptorRepository;
import com.aixoft.escassandra.repository.model.EventDescriptor;
import com.aixoft.escassandra.service.AggregateStore;
import com.aixoft.escassandra.service.EventRouter;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Slf4j
public class CassandraAggregateStore implements AggregateStore {
    EventDescriptorRepository eventDescriptorRepository;
    AggregateSubscribedMethods aggregateSubscribedMethods;
    EventRouter eventRouter;

    @Override
    public boolean save(AggregateRoot aggregate) {
        //TODO: validate if last committed event's version is as expected, if not then throw ConcurrentAggregateModificationException
        //TODO: if some event does not require validation then pulling last event for version could be skipped (consider if snapshot happen then change might not be included)

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

        boolean result = eventDescriptorRepository.insertAll(aggregate.getClass(), aggregate.getId(), newEventDescriptors);

        if(result) {
            aggregate.setCommittedVersion(currentEventVersion);

            for (EventDescriptor eventDescriptor : newEventDescriptors) {
                Event event = eventDescriptor.getEvent();

                //TODO: published aggregate shall be immutable
                apply(event, aggregate);
                eventRouter.publish(event, aggregate);
            }

            aggregate.markChangesAsCommitted();
        }

        return result;
    }

    @Override
    public <T extends AggregateRoot> T findById(UUID aggregateId, Class<T> aggregateClass) {
        T aggregateRoot = createAggregate(aggregateId, aggregateClass);

        List<EventDescriptor> eventDescriptors = eventDescriptorRepository.findAllByAggregateId(aggregateClass, aggregateId);

        loadFromHistory(aggregateRoot, eventDescriptors);

        return aggregateRoot;
    }

    @Override
    public <T extends AggregateRoot> T findById(UUID aggregateId, int baseSnapshotVersion, Class<T> aggregateClass) {
        T aggregateRoot = createAggregate(aggregateId, aggregateClass);

        List<EventDescriptor> eventDescriptors = eventDescriptorRepository.findAllByAggregateIdSinceLastSnapshot(aggregateClass, aggregateId, baseSnapshotVersion);

        loadFromHistory(aggregateRoot, eventDescriptors);

        return aggregateRoot;
    }

    private void loadFromHistory(AggregateRoot aggregateRoot, List<EventDescriptor> eventDescriptors) {
        eventDescriptors.forEach(eventDescriptor -> apply(eventDescriptor.getEvent(), aggregateRoot));

        EventDescriptor lastEventDescriptor = eventDescriptors.get(eventDescriptors.size() - 1);
        aggregateRoot.setCommittedVersion(lastEventDescriptor.getEventVersion());
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
