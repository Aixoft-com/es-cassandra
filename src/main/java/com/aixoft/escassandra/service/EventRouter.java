package com.aixoft.escassandra.service;

import com.aixoft.escassandra.aggregate.AggregateRoot;
import com.aixoft.escassandra.annotation.SubscribeAll;
import com.aixoft.escassandra.model.Event;

/**
 * The interface Event router.
 */
public interface EventRouter {
    /**
     * Register methods annotated with {@link SubscribeAll} and defined in given {@link EventListener} instance.
     *
     * @param eventListener Event listener to be registered.
     */
    void registerEventHandler(EventListener eventListener);

    /**
     * Invokes methods for given event type on registered listeners.
     *
     * @param event     Event to be published.
     * @param publisher Aggregate on which event occurred.
     */
    void publish(Event event, AggregateRoot publisher);
}
