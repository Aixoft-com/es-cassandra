package com.aixoft.escassandra.annotation;

import com.aixoft.escassandra.aggregate.AggregateRoot;

import java.lang.annotation.*;

/**
 * Annotation indicates that method shall be triggered when event with given type was committed:
 * ({@link com.aixoft.escassandra.service.AggregateStore#save(AggregateRoot)}
 * or {@link com.aixoft.escassandra.service.ReactiveAggregateStore#save(AggregateRoot)}).
 * <p>
 * Shall be used on methods in class which extends {@link com.aixoft.escassandra.aggregate.AggregateRoot}.
 * <p>
 * Method shall be public have exactly one parameter with type which implements {@link com.aixoft.escassandra.model.Event}.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Subscribe {
}
