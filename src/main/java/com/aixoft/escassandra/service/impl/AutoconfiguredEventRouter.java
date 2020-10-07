package com.aixoft.escassandra.service.impl;

import com.aixoft.escassandra.aggregate.AggregateRoot;
import com.aixoft.escassandra.annotation.Subscribe;
import com.aixoft.escassandra.exception.runtime.EventHandlerInvocationFailedException;
import com.aixoft.escassandra.exception.runtime.InvalidEventHandlerDefinitionException;
import com.aixoft.escassandra.model.Event;
import com.aixoft.escassandra.service.EventHandler;
import com.aixoft.escassandra.service.EventRouter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class AutoconfiguredEventRouter implements EventRouter {
    private final Map<Class<?>, Map<EventHandler, Method>> eventHandlerMethodByEventClass = new HashMap<>();

    @Override
    public void registerEventHandler(@NonNull EventHandler eventHandler) {
        List<Method> methods = Arrays.stream(eventHandler.getClass().getDeclaredMethods())
            .filter(method -> method.isAnnotationPresent(Subscribe.class))
            .collect(Collectors.toList());

        for (Method method : methods) {
            if (method.getParameterCount() != 2) {
                throw new InvalidEventHandlerDefinitionException(
                    String.format("Method '%s' in class '%s' annotated with '%s' has %d parameters but exactly two are allowed",
                        method.getName(),
                        eventHandler.getClass().getName(),
                        Subscribe.class.getName(),
                        method.getParameterCount())
                );
            }

            Class<?> publisherParameterType = method.getParameterTypes()[1];
            if (!AggregateRoot.class.isAssignableFrom(publisherParameterType)) {
                throw new InvalidEventHandlerDefinitionException(
                    String.format("Method '%s' in class '%s' annotated with '%s' has 2nd parameter which in not subtype of '%s'",
                        method.getName(),
                        eventHandler.getClass().getName(),
                        Subscribe.class.getName(),
                        AggregateRoot.class.getName())
                );
            }

            Class<?> eventParameterType = method.getParameterTypes()[0];
            if (!Event.class.isAssignableFrom(eventParameterType)) {
                throw new InvalidEventHandlerDefinitionException(
                    String.format("Method '%s' in class '%s' annotated with '%s' has 1st parameter which in not subtype of '%s'",
                        method.getName(),
                        eventHandler.getClass().getName(),
                        Subscribe.class.getName(),
                        Event.class.getName())
                );
            }

            method.setAccessible(true);

            Map<EventHandler, Method> eventHandlerMethod = eventHandlerMethodByEventClass.get(eventParameterType);
            if (eventHandlerMethod == null) {
                Map<EventHandler, Method> newEventHandlerMethod = new HashMap<>();
                newEventHandlerMethod.put(eventHandler, method);

                eventHandlerMethodByEventClass.put(eventParameterType, newEventHandlerMethod);
            } else if (!eventHandlerMethod.containsKey(eventHandler)) {
                eventHandlerMethod.put(eventHandler, method);
            } else {
                throw new InvalidEventHandlerDefinitionException(
                    String.format("More then one event handler defined for same event '%s' in class '%s'",
                        eventParameterType.getName(),
                        eventHandler.getClass().getName())
                );
            }
        }
    }

    @Override
    public void publish(@NonNull Event event, @NonNull AggregateRoot publisher) {
        Map<EventHandler, Method> methodByEventHandler = eventHandlerMethodByEventClass.get(event.getClass());

        if (methodByEventHandler != null) {
            methodByEventHandler.forEach((eventHandler, method) -> {
                try {
                    method.invoke(eventHandler, event, publisher);
                } catch (ReflectiveOperationException ex) {
                    log.error(ex.getMessage(), ex);
                    new EventHandlerInvocationFailedException(
                        String.format("Method '%s' in '%s' failed on invoke",
                            method.getName(),
                            eventHandler)
                    );
                }
            });
        }
    }
}
