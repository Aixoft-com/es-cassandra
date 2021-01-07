package com.aixoft.escassandra.benchmark.util;

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

        for(int it = 0; it < count; it++) {
            eventDescriptors.add(new EventDescriptor(eventVersion, new NameChanged("Name_" + it)));
            eventVersion = eventVersion.getNextMinor();
        }

        return eventDescriptors;
    }
}
