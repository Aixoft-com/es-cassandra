package com.aixoft.escassandra.benchmark.util;

import com.aixoft.escassandra.benchmark.model.event.AggregateCreated;
import com.aixoft.escassandra.benchmark.model.event.NameChanged;
import com.aixoft.escassandra.model.EventVersion;
import com.aixoft.escassandra.repository.model.EventDescriptor;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EventDescriptorGenerator {
    public static List<EventDescriptor> generate(int count) {
        List<EventDescriptor> eventDescriptors = new ArrayList<>(count);

        EventVersion eventVersion = EventVersion.initial();

        eventDescriptors.add(new EventDescriptor(eventVersion, new AggregateCreated("name")));
        for(int it = 1; it < count; it++) {
            eventVersion = eventVersion.getNext(true);
            eventDescriptors.add(new EventDescriptor(eventVersion, new NameChanged("Name_" + it)));
        }

        return eventDescriptors;
    }
}
