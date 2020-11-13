package com.aixoft.escassandra.service.impl;

import com.aixoft.escassandra.aggregate.AggregateRoot;
import com.aixoft.escassandra.annotation.SubscribeAll;
import com.aixoft.escassandra.exception.runtime.EventHandlerInvocationFailedException;
import com.aixoft.escassandra.exception.runtime.InvalidEventHandlerDefinitionException;
import com.aixoft.escassandra.model.Event;
import com.aixoft.escassandra.service.EventListener;
import com.aixoft.escassandra.service.EventRouter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * EventRouter is used to register listener ({@link EventListener}) and handle events when published.
 */
@Slf4j
public class AutoconfiguredEventRouter implements EventRouter {
    private final Map<Class<?>, Map<EventListener, Method>> eventHandlerMethodByEventClass = new HashMap<>();

    /**
     * Register methods annotated with {@link SubscribeAll} and defined in given {@link EventListener} instance.
     */
    @Override
    public void registerEventHandler(@NonNull EventListener eventListener) {
        List<Method> methods = Arrays.stream(eventListener.getClass().getDeclaredMethods())
            .filter(method -> method.isAnnotationPresent(SubscribeAll.class))
            .collect(Collectors.toList());

        for (Method method : methods) {
            if (method.getParameterCount() != 2) {
                throw new InvalidEventHandlerDefinitionException(
                    String.format("Method '%s' in class '%s' annotated with '%s' has %d parameters but exactly two are allowed",
                        method.getName(),
                        eventListener.getClass().getName(),
                        SubscribeAll.class.getName(),
                        method.getParameterCount())
                );
            }

            Class<?> publisherParameterType = method.getParameterTypes()[1];
            if (!AggregateRoot.class.isAssignableFrom(publisherParameterType)) {
                throw new InvalidEventHandlerDefinitionException(
                    String.format("Method '%s' in class '%s' annotated with '%s' has 2nd parameter which in not subtype of '%s'",
                        method.getName(),
                        eventListener.getClass().getName(),
                        SubscribeAll.class.getName(),
                        AggregateRoot.class.getName())
                );
            }

            Class<?> eventParameterType = method.getParameterTypes()[0];
            if (!Event.class.isAssignableFrom(eventParameterType)) {
                throw new InvalidEventHandlerDefinitionException(
                    String.format("Method '%s' in class '%s' annotated with '%s' has 1st parameter which in not subtype of '%s'",
                        method.getName(),
                        eventListener.getClass().getName(),
                        SubscribeAll.class.getName(),
                        Event.class.getName())
                );
            }

            Map<EventListener, Method> eventHandlerMethod = eventHandlerMethodByEventClass.get(eventParameterType);
            if (eventHandlerMethod == null) {
                Map<EventListener, Method> newEventHandlerMethod = new HashMap<>();
                newEventHandlerMethod.put(eventListener, method);

                eventHandlerMethodByEventClass.put(eventParameterType, newEventHandlerMethod);
            } else if (!eventHandlerMethod.containsKey(eventListener)) {
                eventHandlerMethod.put(eventListener, method);
            } else {
                throw new InvalidEventHandlerDefinitionException(
                    String.format("More then one event handler defined for same event '%s' in class '%s'",
                        eventParameterType.getName(),
                        eventListener.getClass().getName())
                );
            }
        }
    }

    /**
     * Invokes methods for given event type on registered listeners.
     *
     * @param event - Event to be published.
     * @param publisher Aggregate on which event occurred.
     */
    @Override
    public void publish(@NonNull Event event, @NonNull AggregateRoot publisher) {
        Map<EventListener, Method> methodByEventHandler = eventHandlerMethodByEventClass.get(event.getClass());

        if (methodByEventHandler != null) {
            methodByEventHandler.forEach((eventHandler, method) -> {
                try {
                    method.invoke(eventHandler, event, publisher);
                } catch (ReflectiveOperationException ex) {
                    log.error(ex.getMessage(), ex);
                    throw new EventHandlerInvocationFailedException(
                        String.format("Method '%s' in '%s' failed on invoke",
                            method.getName(),
                            eventHandler)
                    );
                }
            });
        }
    }
}
