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
    String[] aggregatePackages() default {};

    String[] eventPackages() default {};
}
