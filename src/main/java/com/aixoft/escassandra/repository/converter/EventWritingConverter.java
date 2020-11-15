package com.aixoft.escassandra.repository.converter;

import com.aixoft.escassandra.annotation.DomainEvent;
import com.aixoft.escassandra.model.Event;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import org.springframework.core.convert.converter.Converter;

import java.io.IOException;

/**
 * Converts event to JSON String.
 */
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class EventWritingConverter implements Converter<Event, String> {
    ObjectMapper objectMapper;

    /**
     * Serializes event with event name ({@link EventWrapper}) to JSON.
     *
     * @param event Event to be serialized.
     *
     * @return JSON string with serialized event.
     */
    @SneakyThrows(value = IOException.class)
    @Override
    public String convert(Event event) {
        EventWrapper wrapper = new EventWrapper(event.getClass().getAnnotation(DomainEvent.class).event(), event);

        return objectMapper.writeValueAsString(wrapper);
    }
}
