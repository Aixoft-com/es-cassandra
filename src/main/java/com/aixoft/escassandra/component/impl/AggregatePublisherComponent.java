package com.aixoft.escassandra.component.impl;

import com.aixoft.escassandra.aggregate.AggregateRoot;
import com.aixoft.escassandra.component.AggregatePublisher;
import com.aixoft.escassandra.component.AggregateSubscribedMethods;
import com.aixoft.escassandra.model.Event;
import com.aixoft.escassandra.repository.model.EventDescriptor;
import com.aixoft.escassandra.service.EventRouter;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class AggregatePublisherComponent implements AggregatePublisher {
    AggregateSubscribedMethods aggregateSubscribedMethods;
    EventRouter eventRouter;

    @Override
    public <T extends AggregateRoot> T applyAndPublish(T aggregate, List<EventDescriptor> eventDescriptors) {
        eventDescriptors.forEach( eventDescriptor -> {

            //TODO: published aggregate shall be immutable
            apply(aggregate, eventDescriptor);

            eventRouter.publish(eventDescriptor.getEvent(), aggregate);
        });

        aggregate.markChangesAsCommitted();

        return aggregate;
    }

    @Override
    public <T extends AggregateRoot> T apply(T aggregate, EventDescriptor eventDescriptor) {
        aggregateSubscribedMethods.invokeAggregateMethodForEvent(aggregate, eventDescriptor.getEvent());
        aggregate.setCommittedVersion(eventDescriptor.getEventVersion());

        return aggregate;
    }
}
