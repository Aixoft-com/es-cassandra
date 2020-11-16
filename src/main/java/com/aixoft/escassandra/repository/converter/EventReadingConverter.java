package com.aixoft.escassandra.repository.converter;

import com.aixoft.escassandra.component.registrar.DomainEventsComponent;
import com.aixoft.escassandra.model.Event;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import org.springframework.core.convert.converter.Converter;

import java.io.IOException;

/**
 * Converts JSON String to Event using mapping from {@link DomainEventsComponent}
 */
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class EventReadingConverter implements Converter<String, Event> {
    DomainEventsComponent domainEventsConfiguration;
    ObjectMapper objectMapper;

    /**
     * Converts JSON String to Event.
     *
     * @param eventWrapperString JSON serialized {@link com.aixoft.escassandra.repository.converter.EventWrapper}.
     *
     * @return Deserialized event.
     */
    @SneakyThrows(value = IOException.class)
    @Override
    public Event convert(@NonNull String eventWrapperString) {
        JsonNode rootNode = objectMapper.reader().readTree(eventWrapperString);
        String eventName = rootNode.at("/event").asText();

        JsonNode data = rootNode.at("/data");
        return objectMapper.readValue(data.traverse(), domainEventsConfiguration.getEventClassByName(eventName));
    }
}
