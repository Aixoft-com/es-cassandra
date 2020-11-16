package com.aixoft.escassandra.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation describes event name which will be used for persistence in database.
 * Event name shall be unique for given aggregate type.
 * <p>
 * Annotation shall be used on classes which implement {@link com.aixoft.escassandra.model.Event} interface.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DomainEvent {
    /**
     * Event name.
     *
     * @return String with Event name.
     */
    String event();
}
