package com.aixoft.escassandra.component;

import com.aixoft.escassandra.aggregate.AggregateRoot;
import com.aixoft.escassandra.model.Event;

/**
 * The interface Aggregate subscribed methods.
 */
public interface AggregateSubscribedMethods {
    /**
     * Invokes aggregate subscribed method annotated with {@link com.aixoft.escassandra.annotation.Subscribe}
     *
     * @param aggregateRoot Aggregate on which event was published.
     * @param event         Event which is sent as parameter on invoke.
     */
    void invokeAggregateMethodForEvent(AggregateRoot aggregateRoot, Event event);
}
