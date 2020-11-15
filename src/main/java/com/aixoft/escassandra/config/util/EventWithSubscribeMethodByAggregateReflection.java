package com.aixoft.escassandra.config.util;

import com.aixoft.escassandra.aggregate.AggregateRoot;
import com.aixoft.escassandra.annotation.Subscribe;
import com.aixoft.escassandra.exception.runtime.InvalidSubscribedMethodDefinitionException;
import com.aixoft.escassandra.model.Event;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Scans for event subscribed methods.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EventWithSubscribeMethodByAggregateReflection {

    /**
     * Scans aggregate class for methods annotated with {@link Subscribe} and creates
     * map of methods by event and aggregate class.
     *<p>
     * If subscribed method takes more then one parameter
     * or parameter is not subtype of {@link Event}
     * or there is more then one subscribed method with same signature
     * then {@link InvalidSubscribedMethodDefinitionException} is thrown.
     *
     * @param aggregateClasses Aggregate classes to be scanned for subscribed methods.
     *
     * @return Map of methods by event and aggregate class.
     *
     * @throws InvalidSubscribedMethodDefinitionException If definition of subscribed method is invalid.
     */
    public static Map<Class<? extends AggregateRoot>, Map<Class<?>, Method>> find(@NonNull List<Class<? extends AggregateRoot>> aggregateClasses) {
        Map<Class<? extends AggregateRoot>, Map<Class<?>, Method>> eventWithSubscribedMethodByAggregateRoot = new HashMap<>();

        aggregateClasses.forEach(aggregateClass -> Arrays.stream(aggregateClass.getDeclaredMethods())
            .filter(method -> method.isAnnotationPresent(Subscribe.class))
            .forEach(method -> addAggregateSubscribedMethodsToMap(aggregateClass, method, eventWithSubscribedMethodByAggregateRoot)));

        return eventWithSubscribedMethodByAggregateRoot;
    }

    private static void addAggregateSubscribedMethodsToMap(Class<? extends AggregateRoot> aggregateClass,
                                                           Method method, Map<Class<? extends AggregateRoot>,
        Map<Class<?>, Method>> eventWithHandlerMethodByAggregateRoot) {

        if (method.getParameterCount() != 1) {
            throw new InvalidSubscribedMethodDefinitionException(
                String.format("Method '%s' in class '%s' annotated with '%s' has %d parameters but only one is allowed",
                    method.getName(),
                    aggregateClass.getName(),
                    Subscribe.class.getName(),
                    method.getParameterCount())
            );
        }

        Class<?> eventParameterType = method.getParameterTypes()[0];
        if (!Event.class.isAssignableFrom(eventParameterType)) {
            throw new InvalidSubscribedMethodDefinitionException(
                String.format("Method '%s' in class '%s' annotated with '%s' has parameter which in not subtype of '%s'",
                    method.getName(),
                    aggregateClass.getName(),
                    Subscribe.class.getName(),
                    Event.class.getName())
            );
        }

        Map<Class<?>, Method> eventHandlerWithMethod = eventWithHandlerMethodByAggregateRoot.get(aggregateClass);

        if (eventHandlerWithMethod == null) {
            Map<Class<?>, Method> newEventWithHandlerMethod = new HashMap<>();
            newEventWithHandlerMethod.put(eventParameterType, method);

            eventWithHandlerMethodByAggregateRoot.put(aggregateClass, newEventWithHandlerMethod);
        } else if (!eventHandlerWithMethod.containsKey(eventParameterType)) {
            eventHandlerWithMethod.put(eventParameterType, method);
        } else {
            throw new InvalidSubscribedMethodDefinitionException(
                String.format("More then one event handler defined for same event '%s' in class '%s'",
                    eventParameterType.getName(),
                    aggregateClass.getName())
            );
        }
    }
}
