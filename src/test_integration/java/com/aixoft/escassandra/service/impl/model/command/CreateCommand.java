package com.aixoft.escassandra.service.impl.model.command;

import com.aixoft.escassandra.aggregate.Aggregate;
import com.aixoft.escassandra.model.Command;
import com.aixoft.escassandra.model.Event;
import com.aixoft.escassandra.service.impl.model.AggregateDataMock;
import com.aixoft.escassandra.service.impl.model.event.AggregateCreated;
import lombok.Value;

import java.util.List;

@Value
public class CreateCommand implements Command<AggregateDataMock> {
    String name;

    @Override
    public Iterable<Event<AggregateDataMock>> toEvents(Aggregate<AggregateDataMock> aggregate) {
        return List.of(new AggregateCreated(name));
    }
}
