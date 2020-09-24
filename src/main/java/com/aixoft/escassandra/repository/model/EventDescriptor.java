package com.aixoft.escassandra.repository.model;

import com.aixoft.escassandra.model.Event;
import lombok.Value;

import java.util.UUID;

@Value
public class EventDescriptor {
    int majorVersion;
    int minorVersion;
    UUID eventId;
    Event event;
}
