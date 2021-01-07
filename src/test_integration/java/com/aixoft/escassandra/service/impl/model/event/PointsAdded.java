package com.aixoft.escassandra.service.impl.model.event;

import com.aixoft.escassandra.annotation.DomainEvent;
import com.aixoft.escassandra.model.AggregateUpdater;
import com.aixoft.escassandra.model.Event;
import com.aixoft.escassandra.service.impl.model.AggregateDataMock;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

@Value
@DomainEvent(event = "PointsAdded")
public class PointsAdded implements Event<AggregateDataMock> {
    int points;

    @JsonCreator
    public PointsAdded(@JsonProperty("points") int points) {
        this.points = points;
    }

    @Override
    public AggregateUpdater<AggregateDataMock> createUpdater() {
        return obj -> new AggregateDataMock(obj.getUserName(), obj.getPoints() + points);
    }
}
