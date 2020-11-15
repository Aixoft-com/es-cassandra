package com.aixoft.escassandra.annotation;

import com.aixoft.escassandra.config.CassandraEventSourcingBeansRegistrar;
import com.aixoft.escassandra.config.EsCommonConfiguration;
import com.aixoft.escassandra.config.EsConfiguration;
import com.aixoft.escassandra.config.EsReactiveConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Autoconfigure EsCassandra application.
 * Specifies packages scanned for aggregates and events.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import({
    CassandraEventSourcingBeansRegistrar.class,
    EsCommonConfiguration.class,
    EsConfiguration.class,
    EsReactiveConfiguration.class
})
@Configuration
public @interface EnableCassandraEventSourcing {
    /**
     * List of packages scanned for classes which extend {@link com.aixoft.escassandra.aggregate.AggregateRoot}
     * and annotated with {@link com.aixoft.escassandra.annotation.Aggregate}.
     *
     * @return Aggregate packages to be scanned.
     */
    String[] aggregatePackages() default {};

    /**
     * List of packages scanned for classes which implement {@link com.aixoft.escassandra.model.Event}
     * and annotated with {@link com.aixoft.escassandra.annotation.DomainEvent}.
     *
     * @return Event packages to be scanned.
     */
    String[] eventPackages() default {};
}
