package com.aixoft.escassandra.repository.converter;

import com.aixoft.escassandra.model.Event;
import lombok.Value;

/**
 * Event wrapper used for data serialization.
 */
@Value
class EventWrapper {
    /**
     * Event name.
     */
    String event;

    /**
     * Wrapped event.
     */
    Event data;
}
