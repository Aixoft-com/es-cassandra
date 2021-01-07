package com.aixoft.escassandra.service.impl.model.command;

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
    public List<Event<AggregateDataMock>> toEvents() {
        return List.of(new AggregateCreated(name));
    }
}
