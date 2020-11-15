package com.aixoft.escassandra.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation describes table name which will be used for aggregate persistence in database.
 * <p>
 * Table name shall be unique for each aggregate type.
 * It shall contain only alphanumerical characters including '_'.
 * <p>
 * Annotation shall be used on classes which extend {@link com.aixoft.escassandra.aggregate.AggregateRoot} base class.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Aggregate {
    /**
     * Name of cassandra table to persist aggregate.
     *
     * @return Cassandra table name.
     */
    String tableName();
}
