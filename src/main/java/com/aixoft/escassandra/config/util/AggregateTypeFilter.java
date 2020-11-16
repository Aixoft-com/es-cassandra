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

/**
 * Reflection based aggregate filter.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class AggregateTypeFilter {
    /**
     * Scan base packages and filter all aggregate classes which can be assigned from given class.
     *
     * @param basePackages    Aggregate base packages.
     * @param assignableClass Class from which filtered class can be assigned.
     *
     * @return Filtered aggregate classes.
     */
    public static List<Class<? extends AggregateRoot>> filterAllAggregateClasses(@NonNull String[] basePackages, @NonNull Class<? extends AggregateRoot> assignableClass) {

        final ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(
            true);
        provider.addIncludeFilter(new AssignableTypeFilter(AggregateRoot.class));

        List<Class<? extends AggregateRoot>> filteredClasses = new ArrayList<>();

        Arrays.stream(basePackages).forEach(basePackage -> filteredClasses
            .addAll(provider.findCandidateComponents(basePackage).stream()
                .map(AggregateTypeFilter::getClassByBeanDefinition)
                .filter(assignableClass::isAssignableFrom)
                .collect(Collectors.toList())
            )
        );

        return filteredClasses;
    }


    private static Class<? extends AggregateRoot> getClassByBeanDefinition(BeanDefinition beanDefinition) {
        try {
            return (Class<? extends AggregateRoot>) Class.forName(beanDefinition.getBeanClassName());
        } catch (ClassNotFoundException ex) {
            log.error(ex.getMessage(), ex);
            throw new ClassNotFoundByBeanDefinitionException(String.format("Not able to find class by name %s", beanDefinition.getBeanClassName()));
        }
    }
}
