package com.aixoft.escassandra.config.util;

import com.aixoft.escassandra.annotation.DomainEvent;
import com.aixoft.escassandra.model.Event;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.springframework.beans.BeanInstantiationException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EventClassByNameReflection {
    public static final Map<String, Class<? extends Event>> find(@NonNull String[] basePackages) {
        Map<String, Class<? extends Event>> eventClassByName = new HashMap<>();

        final ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(true);
        provider.addIncludeFilter(new AnnotationTypeFilter(DomainEvent.class));

        Arrays.stream(basePackages).forEach(basePackage -> {
            addEventsByNameInPackage(eventClassByName, provider, basePackage);
        });

        return eventClassByName;
    }

    @SneakyThrows
    private static void addEventsByNameInPackage(Map<String, Class<? extends Event>> domainEventMap, ClassPathScanningCandidateComponentProvider provider, String basePackage) {
        for (BeanDefinition beanDefinition : provider.findCandidateComponents(basePackage)) {
            Class eventClass = Class.forName(beanDefinition.getBeanClassName());

            Annotation annotation = eventClass.getAnnotation(DomainEvent.class);
            if (annotation instanceof DomainEvent) {
                DomainEvent domainEvent = (DomainEvent) annotation;

                if (domainEvent.event() == null || domainEvent.event().isBlank()) {
                    throw new BeanInstantiationException(eventClass, String.format("Event is null or blank"));
                }

                if (domainEventMap.containsKey(domainEvent.event())) {
                    throw new BeanInstantiationException(eventClass, String.format("Duplicated event '%s'", domainEvent.event()));
                }

                domainEventMap.put(domainEvent.event(), eventClass);
            }
        }
    }
}
