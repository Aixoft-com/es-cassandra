package com.aixoft.escassandra.benchmark.model.command;

import com.aixoft.escassandra.aggregate.Aggregate;
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
    public Iterable<Event<AggregateDataMock>> toEvents(Aggregate<AggregateDataMock> aggregate) {
        return List.of(new NameChanged(name));
    }
}
