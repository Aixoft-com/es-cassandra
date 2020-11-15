package com.aixoft.escassandra.component.registrar;

import com.aixoft.escassandra.component.DomainEvents;
import com.aixoft.escassandra.config.util.EventClassByNameReflection;
import com.aixoft.escassandra.model.Event;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.util.Map;

/**
 * Contains mapping between event type and its name used for deserialization.
 * See {@link com.aixoft.escassandra.annotation.DomainEvent}.
 */
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class DomainEventsComponent implements DomainEvents {
    Map<String, Class<? extends Event>> eventClassByName;

    /**
     * Instantiates a new Domain events component.
     *
     * @param basePackages the base packages
     */
    public DomainEventsComponent(String[] basePackages) {
        eventClassByName = EventClassByNameReflection.find(basePackages);
    }

    /**
     * Get Class of the event by event name.
     * See {@link com.aixoft.escassandra.annotation.DomainEvent#event()}.
     *
     * @param name Name of the event used.
     *
     * @return Event class by event name.
     */
    @Override
    public Class<? extends Event> getEventClassByName(String name) {
        return eventClassByName.get(name);
    }
}
