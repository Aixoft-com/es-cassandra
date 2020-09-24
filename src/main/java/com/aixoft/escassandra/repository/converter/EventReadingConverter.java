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

@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class EventReadingConverter implements Converter<String, Event> {
    DomainEventsComponent domainEventsConfiguration;
    ObjectMapper objectMapper;

    @SneakyThrows(value = IOException.class)
    @Override
    public Event convert(@NonNull String eventWrapper) {
        JsonNode rootNode = objectMapper.reader().readTree(eventWrapper);
        String eventName = rootNode.at("/event").asText();

        JsonNode data = rootNode.at("/data");
        return objectMapper.readValue(data.traverse(), domainEventsConfiguration.getEventClassByName(eventName));
    }
}
