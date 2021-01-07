package com.aixoft.escassandra.service.impl.model.event;

import com.aixoft.escassandra.annotation.DomainEvent;
import com.aixoft.escassandra.model.AggregateUpdater;
import com.aixoft.escassandra.model.Event;
import com.aixoft.escassandra.service.impl.model.AggregateDataMock;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

@Value
@DomainEvent(event = "AggregateCreated")
public class AggregateCreated implements Event<AggregateDataMock> {
    String userName;

    @JsonCreator
    public AggregateCreated(@JsonProperty("userName") String userName) {
        this.userName = userName;
    }

    @Override
    public AggregateUpdater<AggregateDataMock> createUpdater() {
        return obj -> new AggregateDataMock(userName, 0);
    }
}
