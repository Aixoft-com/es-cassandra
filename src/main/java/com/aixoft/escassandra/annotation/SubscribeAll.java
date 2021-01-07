package com.aixoft.escassandra.annotation;

import com.aixoft.escassandra.aggregate.Aggregate;

import java.lang.annotation.*;

/**
 * Annotation indicates that method shall be triggered when event with given type was committed:
 * ({@link com.aixoft.escassandra.service.AggregateStore#save(Aggregate)}
 * or {@link com.aixoft.escassandra.service.ReactiveAggregateStore#save(Aggregate)}).
 * <p>
 * Shall be used on methods in class which implements {@link com.aixoft.escassandra.service.EventListener}.
 * <p>
 * Method shall be public have exactly 3 parameters.
 * First parameter type of the method shall implement {@link com.aixoft.escassandra.model.Event}
 * Second parameter type of the method indicates event version (See {@link com.aixoft.escassandra.model.EventVersion}).
 * Third parameter type of the method indicates aggregate id (See {@link java.util.UUID}).
 * <p>
 * Annotation cannot be used on more then one method with same event type in single {@link com.aixoft.escassandra.service.EventListener}.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface SubscribeAll {
}
