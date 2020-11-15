package com.aixoft.escassandra.component;

import com.aixoft.escassandra.model.Event;

/**
 * Interface to get event class by event name.
 */
public interface DomainEvents {
    /**
     * Get Class of the event by event name.
     * See {@link com.aixoft.escassandra.annotation.DomainEvent#event()}.
     *
     * @param name Name of the event used.
     *
     * @return Event class by event name.
     */
    Class<? extends Event> getEventClassByName(String name);
}
