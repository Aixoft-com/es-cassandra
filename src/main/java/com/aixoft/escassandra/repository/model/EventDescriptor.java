package com.aixoft.escassandra.repository.model;

import com.aixoft.escassandra.model.Event;
import com.aixoft.escassandra.model.EventVersion;
import lombok.Value;

import java.util.UUID;

@Value
public class EventDescriptor {
    EventVersion eventVersion;
    Event event;
}
