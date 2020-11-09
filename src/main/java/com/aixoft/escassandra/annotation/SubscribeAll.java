package com.aixoft.escassandra.annotation;

import com.aixoft.escassandra.aggregate.AggregateRoot;
import com.aixoft.escassandra.model.Event;
import com.aixoft.escassandra.service.EventListener;

import java.lang.annotation.*;

/**
 * Annotation indicates that method shall be trigger when event with given type was published.
 *
 * Shall be used on methods in class which implements {@link EventListener}.
 *
 * Method shall have exactly two parameters.
 * First parameter type of the method shall implement {@link Event}
 * Second parameter type of the method shall be of {@link AggregateRoot} type.
 *
 * Annotation cannot be used on more then one method with same event type in single {@link EventListener}.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface SubscribeAll {
}
