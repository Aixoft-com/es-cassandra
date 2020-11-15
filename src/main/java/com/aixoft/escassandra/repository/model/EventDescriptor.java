package com.aixoft.escassandra.repository.model;

import com.aixoft.escassandra.model.Event;
import com.aixoft.escassandra.model.EventVersion;
import com.aixoft.escassandra.model.SnapshotEvent;
import lombok.Value;

import java.util.ArrayList;
import java.util.List;

/**
 * Pair of {@link Event} and {@link EventVersion}.
 */
@Value
public class EventDescriptor {
    EventVersion eventVersion;
    Event event;

    /**
     * Converts {@link Event} list to event descriptors.
     * Version is generated for each event descriptor based on {@link EventVersion#getNext(boolean)} starting from
     * version indicated by {@code startVersion}.
     * <p>
     * If {@code startVersion} is 'null' then {@link EventVersion#initial()} will be used as start Version.
     *
     * @param events       List of events to be converted.
     * @param startVersion Starting event version. First event descriptor will have next value. Nullable.
     * @return List of event descriptors.
     */
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
