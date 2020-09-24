package com.aixoft.escassandra.component;

import com.aixoft.escassandra.aggregate.AggregateRoot;
import com.aixoft.escassandra.model.Event;

public interface AggregateSubscribedMethods {
    void invokeAggregateMethodForEvent(AggregateRoot aggregateRoot, Event event);
}
