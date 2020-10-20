package com.aixoft.escassandra.repository.util;

import com.aixoft.escassandra.model.Event;
import com.aixoft.escassandra.model.EventVersion;
import com.aixoft.escassandra.repository.model.EventDescriptor;
import com.datastax.oss.driver.api.core.cql.Row;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.core.convert.converter.Converter;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EventDescriptorRowUtil {
    public static EventDescriptor toEventDescriptor(Row row, Converter<String, Event> jsonToEventConverter) {
        return new EventDescriptor(
            new EventVersion(row.getInt(1), row.getInt(2)),
            jsonToEventConverter.convert(row.getString(3))
        );
    }
}
