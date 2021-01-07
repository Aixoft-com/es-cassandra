package com.aixoft.escassandra.service;

import com.aixoft.escassandra.annotation.SubscribeAll;
import com.aixoft.escassandra.model.Event;
import com.aixoft.escassandra.model.EventVersion;

import java.util.UUID;

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
     * @param <T>         Aggregate data type.
     * @param event       Event to be published.
     * @param version     Version of the event.
     * @param aggregateId UUID of the aggregate.
     */
    <T> void publish(Event<T> event, EventVersion version, UUID aggregateId);
}
