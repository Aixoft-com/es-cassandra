package com.aixoft.escassandra.benchmark.model.command;

import com.aixoft.escassandra.benchmark.model.AggregateDataMock;
import com.aixoft.escassandra.benchmark.model.event.NameChanged;
import com.aixoft.escassandra.model.Command;
import com.aixoft.escassandra.model.Event;
import lombok.Value;

import java.util.List;

@Value
public class ChangeNameCommand implements Command<AggregateDataMock> {
    String name;

    @Override
    public List<Event<AggregateDataMock>> toEvents() {
        return List.of(new NameChanged(name));
    }
}
