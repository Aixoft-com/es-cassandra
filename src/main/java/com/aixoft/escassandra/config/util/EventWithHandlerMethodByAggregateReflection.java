package com.aixoft.escassandra.config.util;

import com.aixoft.escassandra.aggregate.AggregateRoot;
import com.aixoft.escassandra.annotation.Subscribe;
import com.aixoft.escassandra.exception.runtime.InvalidEventHandlerDefinitionException;
import com.aixoft.escassandra.model.Event;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EventWithHandlerMethodByAggregateReflection {
    public static Map<Class<? extends AggregateRoot>, Map<Class<?>, Method>> find(@NonNull List<Class> aggregateClasses) {
        Map<Class<? extends AggregateRoot>, Map<Class<?>, Method>> eventWithHandlerMethodByAggregateRoot = new HashMap<>();

        aggregateClasses.forEach(aggregateClass -> {
            Arrays.stream(aggregateClass.getDeclaredMethods())
                    .filter(method -> method.isAnnotationPresent(Subscribe.class))
                    .forEach(method -> addAggregateSubscribedMethodsToMap(aggregateClass, method, eventWithHandlerMethodByAggregateRoot));
        });

        return eventWithHandlerMethodByAggregateRoot;
    }


    private static void addAggregateSubscribedMethodsToMap(Class<? extends AggregateRoot> aggregateClass,
                                                    Method method, Map<Class<? extends AggregateRoot>,
            Map<Class<?>, Method>> eventWithHandlerMethodByAggregateRoot) {

        if (method.getParameterCount() != 2) {
            throw new InvalidEventHandlerDefinitionException(
                    String.format("Method '%s' in class '%s' annotated with '%s' has %d parameters but exactly two are allowed",
                            method.getName(),
                            aggregateClass.getName(),
                            Subscribe.class.getName(),
                            method.getParameterCount())
            );
        }

        Class<?> publisherParameterType = method.getParameterTypes()[1];
        if (!AggregateRoot.class.isAssignableFrom(publisherParameterType)) {
            throw new InvalidEventHandlerDefinitionException(
                    String.format("Method '%s' in class '%s' annotated with '%s' has 2nd parameter which in not subtype of '%s'",
                            method.getName(),
                            aggregateClass.getName(),
                            Subscribe.class.getName(),
                            AggregateRoot.class.getName())
            );
        }

        Class<?> eventParameterType = method.getParameterTypes()[0];
        if (!Event.class.isAssignableFrom(eventParameterType)) {
            throw new InvalidEventHandlerDefinitionException(
                    String.format("Method '%s' in class '%s' annotated with '%s' has 1st parameter which in not subtype of '%s'",
                            method.getName(),
                            aggregateClass.getName(),
                            Subscribe.class.getName(),
                            Event.class.getName())
            );
        }

        method.setAccessible(true);

        Map<Class<?>, Method> eventHandlerWithMethod = eventWithHandlerMethodByAggregateRoot.get(aggregateClass);

        if (eventHandlerWithMethod == null) {
            Map<Class<?>, Method> newEventWithHandlerMethod = new HashMap<>();
            newEventWithHandlerMethod.put(eventParameterType, method);

            eventWithHandlerMethodByAggregateRoot.put(aggregateClass, newEventWithHandlerMethod);
        } else if (!eventHandlerWithMethod.containsKey(eventParameterType)) {
            eventHandlerWithMethod.put(eventParameterType, method);
        } else {
            throw new InvalidEventHandlerDefinitionException(
                    String.format("More then one event handler defined for same event '%s' in class '%s'",
                            eventParameterType.getName(),
                            aggregateClass.getName())
            );
        }
    }
}
