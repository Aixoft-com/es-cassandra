package com.aixoft.escassandra.config.util;

import com.aixoft.escassandra.annotation.AggregateData;
import com.aixoft.escassandra.exception.runtime.ClassNotFoundByBeanDefinitionException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

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
     * Scan base packages and filter all aggregate data classes which can be assigned from given class.
     *
     * @param basePackages  Aggregate data base packages.
     *
     * @return Filtered aggregate data classes.
     */
    public static List<Class<?>> filterAllAggregateDataClasses(@NonNull String[] basePackages) {

        final ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(
            true);
        provider.addIncludeFilter(new AnnotationTypeFilter(AggregateData.class));

        List<Class<?>> filteredClasses = new ArrayList<>();

        Arrays.stream(basePackages).forEach(basePackage -> filteredClasses
            .addAll(provider.findCandidateComponents(basePackage).stream()
                .map(AggregateTypeFilter::getClassByBeanDefinition)
                .filter(foundClass -> foundClass.getAnnotation(AggregateData.class) != null)
                .collect(Collectors.toList())
            )
        );

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
