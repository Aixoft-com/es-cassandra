package com.aixoft.escassandra.repository.model;

import com.aixoft.escassandra.model.Event;
import com.aixoft.escassandra.model.EventVersion;
import lombok.AllArgsConstructor;
import lombok.Value;

/**
 * Pair of {@link Event} and {@link EventVersion}.
 */
@Value
@AllArgsConstructor
public class EventDescriptor {
    EventVersion eventVersion;
    Event event;
}
