package com.aixoft.escassandra.component.registrar;

import com.aixoft.escassandra.component.DomainEvents;
import com.aixoft.escassandra.config.util.EventClassByNameReflection;
import com.aixoft.escassandra.model.Event;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.util.Map;

@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class DomainEventsComponent implements DomainEvents {
    Map<String, Class<? extends Event>> eventClassByName;

    public DomainEventsComponent(String[] basePackages) {
        eventClassByName = EventClassByNameReflection.find(basePackages);
    }

    @Override
    public Class<? extends Event> getEventClassByName(String name) {
        return eventClassByName.get(name);
    }
}
