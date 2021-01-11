package com.aixoft.escassandra.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Interface to indicate the lister of events.
 * <p>
 * Class which this annotation can be scanned for
 * {@link com.aixoft.escassandra.annotation.SubscribeAll} annotations during listener registration.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface EventListener {
}
