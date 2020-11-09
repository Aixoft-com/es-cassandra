package com.aixoft.escassandra.component.impl;

import com.aixoft.escassandra.aggregate.AggregateRoot;
import com.aixoft.escassandra.component.AggregateSubscribedMethods;
import com.aixoft.escassandra.component.registrar.AggregateComponent;
import com.aixoft.escassandra.config.util.EventWithHandlerMethodByAggregateReflection;
import com.aixoft.escassandra.model.Event;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;

import java.lang.reflect.Method;
import java.util.Map;

@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class AggregateSubscribedMethodsComponent implements AggregateSubscribedMethods {
    Map<Class<? extends AggregateRoot>, Map<Class<?>, Method>> eventWithHandlerMethodByAggregateRoot;

    AggregateSubscribedMethodsComponent(@NonNull AggregateComponent aggregateComponent) {
        eventWithHandlerMethodByAggregateRoot = EventWithHandlerMethodByAggregateReflection.find(aggregateComponent.getClasses());
    }

    @SneakyThrows
    @Override
    public void invokeAggregateMethodForEvent(@NonNull AggregateRoot aggregateRoot, @NonNull Event event) {
        Map<Class<?>, Method> eventHandlerWithMethod = eventWithHandlerMethodByAggregateRoot.get(aggregateRoot.getClass());

        if (eventHandlerWithMethod != null) {
            Method handlerMethod = eventHandlerWithMethod.get(event.getClass());

            if (handlerMethod != null) {
                handlerMethod.invoke(aggregateRoot, event);
            }
        }
    }
}
