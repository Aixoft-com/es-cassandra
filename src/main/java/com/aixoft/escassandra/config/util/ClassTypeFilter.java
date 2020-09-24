package com.aixoft.escassandra.config.util;

import com.aixoft.escassandra.aggregate.AggregateRoot;
import com.aixoft.escassandra.exception.runtime.ClassNotFoundByBeanDefinitionException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class ClassTypeFilter {
    public static List<Class> filterAllAggregateClasses(@NonNull String[] basePackages, @NonNull Class assignableClass) {

        final ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(
                true);
        provider.addIncludeFilter(new AssignableTypeFilter(AggregateRoot.class));

        List<Class> filteredClasses = new ArrayList<>();

        Arrays.stream(basePackages).forEach(basePackage -> {
            filteredClasses.addAll(provider.findCandidateComponents(basePackage).stream()
                    .map(ClassTypeFilter::getClassByBeanDefinition)
                    .filter(assignableClass::isAssignableFrom)
                    .collect(Collectors.toList())
            );
        });

        return filteredClasses;
    }


    private static Class<?> getClassByBeanDefinition(BeanDefinition beanDefinition) {
        try {
            return Class.forName(beanDefinition.getBeanClassName());
        } catch (ClassNotFoundException ex) {
            log.error(ex.getMessage(), ex);
            throw new ClassNotFoundByBeanDefinitionException(String.format("Not able to find class by name %s", beanDefinition.getBeanClassName()));
        }
    }
}
