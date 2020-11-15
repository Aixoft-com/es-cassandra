package com.aixoft.escassandra.component;

import com.aixoft.escassandra.aggregate.AggregateRoot;
import com.aixoft.escassandra.repository.model.EventDescriptor;

import java.util.List;

/**
 * The interface to perform commit and apply on aggregate.
 */
public interface AggregateCommitter {
    /**
     * Method invokes aggregate event's handlers annotated with {@link com.aixoft.escassandra.annotation.Subscribe}
     * and {@link com.aixoft.escassandra.annotation.SubscribeAll}.
     * <p>
     * Aggregate will be marked as committed.
     * Committed version of the aggregate will be updated based on last event.
     *
     * @param <T>              Type of Aggregate class.
     * @param aggregate        Aggregate on which event will be applied.
     * @param eventDescriptors EventDescriptors containing events to be applied and published.
     *
     * @return Updated Aggregate.
     */
    <T extends AggregateRoot> T commit(T aggregate, List<EventDescriptor> eventDescriptors);

    /**
     * Method invokes event subscribed method annotated with {@link com.aixoft.escassandra.annotation.Subscribe}.
     * Committed version of the aggregate is updated according to version from eventDescriptor.
     *
     * @param <T>             Type of Aggregate class.
     * @param aggregate       Aggregate on which event will be applied.
     * @param eventDescriptor EventDescriptor containing event to be applied.
     *
     * @return Updated Aggregate.
     */
    <T extends AggregateRoot> T apply(T aggregate, EventDescriptor eventDescriptor);
}
