package com.aixoft.escassandra.benchmark.model.event;

import com.aixoft.escassandra.annotation.DomainEvent;
import com.aixoft.escassandra.benchmark.model.AggregateDataMock;
import com.aixoft.escassandra.model.AggregateUpdater;
import com.aixoft.escassandra.model.Event;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

@Value
@DomainEvent(event = "Renamed")
public class NameChanged implements Event<AggregateDataMock> {
    String name;

    @JsonCreator
    public NameChanged(@JsonProperty("name") String name) {
        this.name = name;
    }

    @Override
    public AggregateUpdater<AggregateDataMock> updater() {
        return obj -> new AggregateDataMock(name);
    }
}
