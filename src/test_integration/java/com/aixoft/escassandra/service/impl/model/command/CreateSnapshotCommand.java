package com.aixoft.escassandra.service.impl.model.command;

import com.aixoft.escassandra.model.SnapshotCommand;
import com.aixoft.escassandra.model.SnapshotEvent;
import com.aixoft.escassandra.service.impl.model.AggregateDataMock;
import com.aixoft.escassandra.service.impl.model.event.SnapshotCreated;
import lombok.Value;

@Value
public class CreateSnapshotCommand implements SnapshotCommand<AggregateDataMock> {
    String userName;
    int points;

    @Override
    public SnapshotEvent toEvent() {
        return new SnapshotCreated(userName, points);
    }
}
