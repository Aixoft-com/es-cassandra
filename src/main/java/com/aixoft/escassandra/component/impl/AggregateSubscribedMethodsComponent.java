package com.aixoft.escassandra.component.impl;

import com.aixoft.escassandra.aggregate.AggregateRoot;
import com.aixoft.escassandra.component.AggregateSubscribedMethods;
import com.aixoft.escassandra.component.registrar.AggregateComponent;
import com.aixoft.escassandra.config.util.EventWithSubscribeMethodByAggregateReflection;
import com.aixoft.escassandra.model.Event;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * Contains information about all subscribed methods ({@link com.aixoft.escassandra.annotation.Subscribe})
 * for each aggregate type.
 */
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class AggregateSubscribedMethodsComponent implements AggregateSubscribedMethods {
    Map<Class<? extends AggregateRoot>, Map<Class<?>, Method>> eventWithSubscribeMethodByAggregateRoot;

    /**
     * Instantiates a new Aggregate subscribed methods component.
     *
     * @param aggregateComponent Aggregate component.
     */
    AggregateSubscribedMethodsComponent(@NonNull AggregateComponent aggregateComponent) {
        eventWithSubscribeMethodByAggregateRoot = EventWithSubscribeMethodByAggregateReflection.find(aggregateComponent.getClasses());
    }

    /**
     * Invokes aggregate subscribed method annotated with {@link com.aixoft.escassandra.annotation.Subscribe}
     * @param aggregateRoot Aggregate on which event was published.
     * @param event         Event which is sent as parameter on invoke.
     */
    @SneakyThrows
    @Override
    public void invokeAggregateMethodForEvent(@NonNull AggregateRoot aggregateRoot, @NonNull Event event) {
        Map<Class<?>, Method> eventHandlerWithMethod = eventWithSubscribeMethodByAggregateRoot.get(aggregateRoot.getClass());

        if (eventHandlerWithMethod != null) {
            Method handlerMethod = eventHandlerWithMethod.get(event.getClass());

            if (handlerMethod != null) {
                handlerMethod.invoke(aggregateRoot, event);
            }
        }
    }
}
