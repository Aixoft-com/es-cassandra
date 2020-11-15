package com.aixoft.escassandra.component.impl;

import com.aixoft.escassandra.aggregate.AggregateRoot;
import com.aixoft.escassandra.component.AggregateCommitter;
import com.aixoft.escassandra.component.AggregateSubscribedMethods;
import com.aixoft.escassandra.repository.model.EventDescriptor;
import com.aixoft.escassandra.service.EventRouter;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

/**
 * The type Aggregate committer component.
 */
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class AggregateCommitterComponent implements AggregateCommitter {
    AggregateSubscribedMethods aggregateSubscribedMethods;
    EventRouter eventRouter;

    /**
     * Method invokes aggregate event's handlers annotated with {@link com.aixoft.escassandra.annotation.Subscribe}
     * and {@link com.aixoft.escassandra.annotation.SubscribeAll}.
     *
     * Aggregate will be marked as committed.
     * Committed version of the aggregate will be updated based on last event.
     *
     * @param <T>               Type of Aggregate class.
     * @param aggregate         Aggregate on which event will be applied.
     * @param eventDescriptors  EventDescriptors containing events to be applied and published.
     *
     * @return Updated Aggregate.
     */
    @Override
    public <T extends AggregateRoot> T commit(T aggregate, List<EventDescriptor> eventDescriptors) {
        eventDescriptors.forEach( eventDescriptor -> {

            //TODO: published aggregate shall be immutable
            apply(aggregate, eventDescriptor);

            eventRouter.publish(eventDescriptor.getEvent(), aggregate);
        });

        aggregate.markEventsAsCommitted();

        return aggregate;
    }

    /**
     * Method invokes event subscribed method annotated with {@link com.aixoft.escassandra.annotation.Subscribe}.
     * Committed version of the aggregate is updated according to version from eventDescriptor.
     *
     * @param <T>               Type of Aggregate class.
     * @param aggregate         Aggregate on which event will be applied.
     * @param eventDescriptor   EventDescriptor containing event to be applied.
     *
     * @return Updated Aggregate.
     */
    @Override
    public <T extends AggregateRoot> T apply(T aggregate, EventDescriptor eventDescriptor) {
        aggregateSubscribedMethods.invokeAggregateMethodForEvent(aggregate, eventDescriptor.getEvent());
        aggregate.setCommittedVersion(eventDescriptor.getEventVersion());

        return aggregate;
    }
}
