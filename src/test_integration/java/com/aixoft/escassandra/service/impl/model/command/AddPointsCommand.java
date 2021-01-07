package com.aixoft.escassandra.service.impl.model.command;

import com.aixoft.escassandra.model.Command;
import com.aixoft.escassandra.model.Event;
import com.aixoft.escassandra.service.impl.model.AggregateDataMock;
import com.aixoft.escassandra.service.impl.model.event.PointsAdded;
import lombok.Value;

import java.util.List;

@Value
public class AddPointsCommand  implements Command<AggregateDataMock> {
    int points;

    @Override
    public List<Event<AggregateDataMock>> toEvents() {
        return List.of(new PointsAdded(points));
    }
}
