package com.aixoft.escassandra.service.impl;

import com.aixoft.escassandra.annotation.SubscribeAll;
import com.aixoft.escassandra.exception.runtime.EventHandlerInvocationFailedException;
import com.aixoft.escassandra.exception.runtime.InvalidSubscribedMethodDefinitionException;
import com.aixoft.escassandra.model.Event;
import com.aixoft.escassandra.model.EventVersion;
import com.aixoft.escassandra.service.EventRouter;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * EventRouter is used to register listener ({@link EventListener}) and handle events when published.
 */
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AutoconfiguredEventRouter implements EventRouter {
    Map<Class<?>, Map<Object, Method>> eventHandlerMethodByEventClass = new HashMap<>();

    /**
     * Returns from App Context all ({@link com.aixoft.escassandra.annotation.EventListener}) and register them
     *
     * @param context ApplicationContext
     */
    public AutoconfiguredEventRouter(ApplicationContext context) {
        context.getBeansWithAnnotation(com.aixoft.escassandra.annotation.EventListener.class).values().forEach(this::registerEventHandler);
    }

    /**
     * Register methods annotated with {@link SubscribeAll} and defined in given {@link EventListener} instance.
     * <p>
     * If number of method parameters is different then three
     * or first parameter is not subtype of {@link Event}
     * or second parameter is not subtype of {@link EventVersion}
     * or third parameter is not subtype of {@link UUID}
     * or signature of subscribed method is duplicated
     * then {@link InvalidSubscribedMethodDefinitionException} is thrown.
     *
     * @throws InvalidSubscribedMethodDefinitionException If definition of subscribed method is invalid.
     */
    private void registerEventHandler(@NonNull Object eventListener) {
        List<Method> methods = Arrays.stream(eventListener.getClass().getDeclaredMethods())
            .filter(method -> method.isAnnotationPresent(SubscribeAll.class))
            .collect(Collectors.toList());

        for (Method method : methods) {
            if (method.getParameterCount() != 3) {
                throw new InvalidSubscribedMethodDefinitionException(
                    String.format("Method '%s' in class '%s' annotated with '%s' has %d parameters but exactly 3 are allowed",
                        method.getName(),
                        eventListener.getClass().getName(),
                        SubscribeAll.class.getName(),
                        method.getParameterCount())
                );
            }

            Class<?> eventParameterType = method.getParameterTypes()[0];
            if (!Event.class.isAssignableFrom(eventParameterType)) {
                throw new InvalidSubscribedMethodDefinitionException(
                    getExceptionMessage(method, eventListener, "1st", Event.class.getName())
                );
            }

            Class<?> versionParameterType = method.getParameterTypes()[1];
            if (!EventVersion.class.isAssignableFrom(versionParameterType)) {
                throw new InvalidSubscribedMethodDefinitionException(
                    getExceptionMessage(method, eventListener, "2nd", EventVersion.class.getName())
                );
            }

            Class<?> idParameterType = method.getParameterTypes()[2];
            if (!UUID.class.isAssignableFrom(idParameterType)) {
                throw new InvalidSubscribedMethodDefinitionException(
                    getExceptionMessage(method, eventListener, "3rd", UUID.class.getName())
                );
            }

            Map<Object, Method> eventHandlerMethod = eventHandlerMethodByEventClass.get(eventParameterType);
            if (eventHandlerMethod == null) {
                Map<Object, Method> newEventHandlerMethod = new HashMap<>();
                newEventHandlerMethod.put(eventListener, method);

                eventHandlerMethodByEventClass.put(eventParameterType, newEventHandlerMethod);
            } else if (!eventHandlerMethod.containsKey(eventListener)) {
                eventHandlerMethod.put(eventListener, method);
            } else {
                throw new InvalidSubscribedMethodDefinitionException(
                    String.format("More then one event handler defined for same event '%s' in class '%s'",
                        eventParameterType.getName(),
                        eventListener.getClass().getName())
                );
            }
        }
    }

    private String getExceptionMessage(Method method, Object eventListener, String parameterNumber, String className) {
        return String.format("Method '%s' in class '%s' annotated with '%s' has %s parameter which in not subtype of '%s'",
            method.getName(),
            eventListener.getClass().getName(),
            SubscribeAll.class.getName(),
            parameterNumber,
            className);
    }

    /**
     * Invokes methods for given event type on registered listeners.
     *
     * @param event         Published event.
     * @param version       Event version.
     * @param aggregateId   Aggregate id.
     */
    @Override
    public <T> void publish(Event<T> event, EventVersion version, UUID aggregateId) {
        Map<Object, Method> methodByEventHandler = eventHandlerMethodByEventClass.get(event.getClass());

        if (methodByEventHandler != null) {
            methodByEventHandler.forEach((eventHandler, method) -> {
                try {
                    method.invoke(eventHandler, event, version, aggregateId);
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
