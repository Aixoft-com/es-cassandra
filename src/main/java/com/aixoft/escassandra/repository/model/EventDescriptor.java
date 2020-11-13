package com.aixoft.escassandra.repository.model;

import com.aixoft.escassandra.model.Event;
import com.aixoft.escassandra.model.EventVersion;
import com.aixoft.escassandra.model.SnapshotEvent;
import lombok.Value;

import java.util.ArrayList;
import java.util.List;

@Value
public class EventDescriptor {
    EventVersion eventVersion;
    Event event;

    public static List<EventDescriptor> fromEvents(List<Event> events, EventVersion startVersion) {
        List<EventDescriptor> eventDescriptors = new ArrayList<>(events.size());

        EventVersion currentEventVersion;
        if (startVersion == null) {
            currentEventVersion = EventVersion.initial();
        } else {
            currentEventVersion = startVersion;
        }

        for (Event event : events) {
            currentEventVersion = currentEventVersion.getNext(event instanceof SnapshotEvent);

            eventDescriptors.add(new EventDescriptor(
                currentEventVersion,
                event)
            );
        }

        return eventDescriptors;
    }
}
