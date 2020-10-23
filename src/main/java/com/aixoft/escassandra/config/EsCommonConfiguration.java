package com.aixoft.escassandra.config;

import com.aixoft.escassandra.component.impl.AggregateSubscribedMethodsComponent;
import com.aixoft.escassandra.component.impl.CassandraSessionComponent;
import com.aixoft.escassandra.component.impl.PreparedStatementsComponent;
import com.aixoft.escassandra.repository.impl.StatementBinderComponent;
import com.aixoft.escassandra.service.impl.AutoconfiguredEventRouter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
    EsCassandraProperties.class,
    CassandraSessionComponent.class,
    AggregateSubscribedMethodsComponent.class,
    PreparedStatementsComponent.class,
    StatementBinderComponent.class,
    AutoconfiguredEventRouter.class})
public class EsCommonConfiguration {
}
