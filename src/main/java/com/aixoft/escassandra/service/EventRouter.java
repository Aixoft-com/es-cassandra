package com.aixoft.escassandra.service;

import com.aixoft.escassandra.aggregate.AggregateRoot;
import com.aixoft.escassandra.model.Event;

public interface EventRouter {
    void registerEventHandler(EventHandler eventHandler);

    void publish(Event event, AggregateRoot publisher);
}
