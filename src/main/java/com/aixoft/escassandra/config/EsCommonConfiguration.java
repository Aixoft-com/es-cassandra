package com.aixoft.escassandra.config;

import com.aixoft.escassandra.component.impl.CassandraSessionComponent;
import com.aixoft.escassandra.component.impl.PreparedStatementsComponent;
import com.aixoft.escassandra.repository.converter.EventReadingConverter;
import com.aixoft.escassandra.repository.converter.EventWritingConverter;
import com.aixoft.escassandra.repository.impl.StatementBinderComponent;
import com.aixoft.escassandra.service.impl.AutoconfiguredEventRouter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Accumulates common EsCassandra configuration.
 */
@Configuration
@Import({
    EsCassandraProperties.class,
    EventReadingConverter.class,
    EventWritingConverter.class,
    CassandraSessionComponent.class,
    PreparedStatementsComponent.class,
    StatementBinderComponent.class,
    AutoconfiguredEventRouter.class})
public class EsCommonConfiguration {
}
