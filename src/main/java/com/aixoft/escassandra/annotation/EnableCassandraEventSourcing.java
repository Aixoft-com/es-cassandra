package com.aixoft.escassandra.annotation;

import com.aixoft.escassandra.component.impl.AggregateSubscribedMethodsComponent;
import com.aixoft.escassandra.component.impl.CassandraSessionComponent;
import com.aixoft.escassandra.config.CassandraEventSourcingBeansRegistrar;
import com.aixoft.escassandra.config.EsCassandraProperties;
import com.aixoft.escassandra.service.impl.AutoconfiguredEventRouter;
import com.aixoft.escassandra.component.impl.PreparedStatementsComponent;
import com.aixoft.escassandra.service.impl.CassandraAggregateStore;
import com.aixoft.escassandra.repository.impl.CassandraEventDescriptorRepository;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import({EsCassandraProperties.class,
        CassandraEventSourcingBeansRegistrar.class,
        CassandraSessionComponent.class,
        AggregateSubscribedMethodsComponent.class,
        PreparedStatementsComponent.class,
        AutoconfiguredEventRouter.class,
        CassandraEventDescriptorRepository.class,
        CassandraAggregateStore.class
})
@Configuration
public @interface EnableCassandraEventSourcing {
    String[] aggregatePackages() default {};
    String[] eventPackages() default {};
}
