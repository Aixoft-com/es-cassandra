package com.aixoft.escassandra.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation describes table name which will be used for aggregate persistence in database.
 * Table name shall be unique for aggregate type.
 * Shall be used on classes which extend {@link com.aixoft.escassandra.aggregate.AggregateRoot} interface.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Aggregate {
    String tableName();
}
