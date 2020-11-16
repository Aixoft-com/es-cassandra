package com.aixoft.escassandra.config.util;

import com.aixoft.escassandra.annotation.DomainEvent;
import com.aixoft.escassandra.exception.runtime.ClassNotFoundByBeanDefinitionException;
import com.aixoft.escassandra.exception.runtime.InvalidDomainEventDefinitionException;
import com.aixoft.escassandra.model.Event;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanInstantiationException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.AssignableTypeFilter;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Scans for events using reflection.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class EventClassByNameReflection {
    /**
     * Scans packages to find {@link Event} annotated with {@link DomainEvent} and creates maps of events by event name
     * (See {@link DomainEvent#event()}).
     *
     * @param basePackages Packages to be scanned for events.
     *
     * @return Map of event classes by its name.
     *
     * @throws InvalidDomainEventDefinitionException If two event classes have the same event name or name is null or blank.
     */
    public static final Map<String, Class<? extends Event>> find(@NonNull String[] basePackages) {
        Map<String, Class<? extends Event>> eventClassByName = new HashMap<>();

        final ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(true);
        provider.addIncludeFilter(new AssignableTypeFilter(Event.class));
        provider.addIncludeFilter(new AnnotationTypeFilter(DomainEvent.class));

        Arrays.stream(basePackages).forEach(
            basePackage -> addEventsByNameInPackage(eventClassByName, provider, basePackage)
        );

        return eventClassByName;
    }

    private static void addEventsByNameInPackage(Map<String, Class<? extends Event>> domainEventMap, ClassPathScanningCandidateComponentProvider provider, String basePackage) {
        for (BeanDefinition beanDefinition : provider.findCandidateComponents(basePackage)) {

            Class<? extends Event> eventClass;
            try {
                eventClass = (Class<? extends Event>) Class.forName(beanDefinition.getBeanClassName());
            } catch (ClassNotFoundException ex) {
                log.error(ex.getMessage(), ex);
                throw new ClassNotFoundByBeanDefinitionException(String.format("Not able to find class by name %s.", beanDefinition.getBeanClassName()));
            }

            Annotation annotation = eventClass.getAnnotation(DomainEvent.class);
            if (annotation instanceof DomainEvent) {
                DomainEvent domainEvent = (DomainEvent) annotation;

                if (domainEvent.event() == null || domainEvent.event().isBlank()) {
                    throw new InvalidDomainEventDefinitionException(String.format("Event is null or blank in %s.", eventClass.getName()));
                }

                if (domainEventMap.containsKey(domainEvent.event())) {
                    throw new InvalidDomainEventDefinitionException(String.format("Duplicated event '%s' in [%s, %s].",
                        domainEvent.event(),
                        eventClass.getName(),
                        domainEventMap.get(domainEvent.event()))
                    );
                }

                domainEventMap.put(domainEvent.event(), eventClass);
            }
        }
    }

}
