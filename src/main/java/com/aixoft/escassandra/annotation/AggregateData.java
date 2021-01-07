package com.aixoft.escassandra.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation describes table name which will be used for aggregate data persistence in database.
 * <p>
 * Table name shall be unique for each aggregate type.
 * It shall contain only alphanumerical characters including '_'.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AggregateData {
    /**
     * Name of cassandra table to persist aggregate data.
     *
     * @return Cassandra table name.
     */
    String tableName();
}
