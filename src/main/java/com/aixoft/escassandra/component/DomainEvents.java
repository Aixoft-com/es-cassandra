package com.aixoft.escassandra.component;

import com.aixoft.escassandra.model.Event;

public interface DomainEvents {
    Class<? extends Event> getEventClassByName(String name);
}
