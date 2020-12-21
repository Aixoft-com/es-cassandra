package com.aixoft.escassandra.benchmark.model;

import com.aixoft.escassandra.aggregate.AggregateRoot;
import com.aixoft.escassandra.annotation.Aggregate;
import com.aixoft.escassandra.annotation.Subscribe;
import com.aixoft.escassandra.benchmark.model.event.AggregateCreated;
import com.aixoft.escassandra.benchmark.model.event.NameChanged;
import com.aixoft.escassandra.model.Event;

import java.util.UUID;

@Aggregate(tableName = "benchmark_aggregate")
public class AggregateMock extends AggregateRoot {
    private String name;

    public AggregateMock(UUID id) {
        super(id);
    }

    @Subscribe
    public void apply(AggregateCreated aggregateCreated) {
        name = aggregateCreated.getName();
    }

    @Subscribe
    public void apply(NameChanged nameChanged) {
        name = nameChanged.getName();
    }

    public void publishEvent(Event event) {
        publish(event);
    }
}


