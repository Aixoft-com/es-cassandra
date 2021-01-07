package com.aixoft.escassandra.service.impl.model.event;

import com.aixoft.escassandra.annotation.DomainEvent;
import com.aixoft.escassandra.model.AggregateUpdater;
import com.aixoft.escassandra.model.SnapshotEvent;
import com.aixoft.escassandra.service.impl.model.AggregateDataMock;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Value;

@DomainEvent(event = "SnapshotCreate")
@Getter
public class SnapshotCreated implements SnapshotEvent<AggregateDataMock> {
    private String userName;
    private int points;

    @JsonCreator
    public SnapshotCreated(@JsonProperty("userName") String userName,
                           @JsonProperty("points") int points) {
        this.userName = userName;
        this.points = points;
    }

    @Override
    public AggregateUpdater<AggregateDataMock> createUpdater() {
        return obj -> new AggregateDataMock(userName, points);
    }
}
