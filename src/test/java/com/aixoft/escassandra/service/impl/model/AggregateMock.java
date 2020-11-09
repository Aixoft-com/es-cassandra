package com.aixoft.escassandra.service.impl.model;

import com.aixoft.escassandra.aggregate.AggregateRoot;
import com.aixoft.escassandra.annotation.Aggregate;
import com.aixoft.escassandra.annotation.DomainEvent;
import com.aixoft.escassandra.annotation.Subscribe;
import com.aixoft.escassandra.model.Event;
import com.aixoft.escassandra.model.SnapshotEvent;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Value;

import java.util.UUID;

@Aggregate(tableName = "test_aggregate_store")
@Getter
public class AggregateMock extends AggregateRoot {
    private String userName;
    private int points;

    public AggregateMock(UUID id) {
        super(id);
    }

    @Subscribe
    public void apply(AggregateInitialized aggregateInitialized) {
        userName = aggregateInitialized.getUserName();
    }

    @Subscribe
    public void apply(PointsAdded pointsAdded) {
        points += pointsAdded.getPoints();
    }

    @Subscribe
    public void apply(SnapshotCreated snapshotCreated) {
        userName = snapshotCreated.getUserName();
        points = snapshotCreated.getPoints();
    }

    public void initialize(String userName) {
        publish(new AggregateInitialized(userName));
    }

    public void addPoints(int points) {
        publish(new PointsAdded(points));
    }

    public void createSnapshot(String userName, int points) {
        publish(new SnapshotCreated(userName, points));
    }
}

@Value
@DomainEvent(event = "AggregateInitialized")
class AggregateInitialized implements Event {
    String userName;

    @JsonCreator
    public AggregateInitialized(@JsonProperty("userName") String userName) {
        this.userName = userName;
    }
}

@Value
@DomainEvent(event = "PointsAdded")
class PointsAdded implements Event {
    int points;

    @JsonCreator
    public PointsAdded(@JsonProperty("points") int points) {
        this.points = points;
    }
}

@Value
@DomainEvent(event = "SnapshotCreate")
class SnapshotCreated implements SnapshotEvent {
    String userName;
    int points;

    @JsonCreator
    public SnapshotCreated(@JsonProperty("userName") String userName,
                           @JsonProperty("points") int points) {
        this.userName = userName;
        this.points = points;
    }
}
