package com.aixoft.escassandra.component;

import com.aixoft.escassandra.aggregate.AggregateRoot;
import com.aixoft.escassandra.repository.model.EventDescriptor;

import java.util.List;

public interface AggregatePublisher {
    <T extends AggregateRoot> T applyAndPublish(T aggregate, List<EventDescriptor> eventDescriptors);

    <T extends AggregateRoot> T apply(T aggregate, EventDescriptor eventDescriptor);
}
