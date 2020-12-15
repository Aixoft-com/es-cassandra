package com.aixoft.escassandra.annotation;

import com.aixoft.escassandra.aggregate.AggregateRoot;

import java.lang.annotation.*;


/**
 * Annotation indicates that method shall be triggered when event with given type was committed:
 * ({@link com.aixoft.escassandra.service.AggregateStore#save(AggregateRoot)}
 * or {@link com.aixoft.escassandra.service.ReactiveAggregateStore#save(AggregateRoot)}).
 * <p>
 * Shall be used on methods in class with annotation {@link EventListener}.
 * <p>
 * Method shall be public have exactly two parameters.
 * First parameter type of the method shall implement {@link com.aixoft.escassandra.model.Event}
 * Second parameter type of the method shall be of {@link com.aixoft.escassandra.aggregate.AggregateRoot} type.
 * <p>
 * Annotation cannot be used on more then one method with same event type in single {@link EventListener}.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface SubscribeAll {
}
