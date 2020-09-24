package com.aixoft.escassandra.repository.converter;

import com.aixoft.escassandra.model.Event;
import lombok.Value;

@Value
class EventWrapper {
    String event;
    Event data;
}
