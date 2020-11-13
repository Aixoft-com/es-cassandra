package com.aixoft.escassandra.annotation;

import java.lang.annotation.*;

/**
 * Annotation indicates that method shall be trigger when event with given type was published.
 * Shall be used on methods in class which extends {@link com.aixoft.escassandra.aggregate.AggregateRoot}.
 *
 * Method shall have exactly one parameter with type which implements {@link com.aixoft.escassandra.model.Event}.
 *
 * Annotation cannot be used on more then one method with same event type in single EventHandler.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Subscribe {
}
