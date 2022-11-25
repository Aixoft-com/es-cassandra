package com.aixoft.escassandra.config;

import com.aixoft.escassandra.annotation.EnableCassandraEventSourcing;
import com.aixoft.escassandra.component.registrar.AggregateDataComponent;
import com.aixoft.escassandra.component.registrar.DomainEventsComponent;
import com.aixoft.escassandra.config.util.AggregateTypeFilter;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationConfigurationException;
import org.springframework.core.type.AnnotationMetadata;

import java.util.List;
import java.util.Map;

/**
 * Registers beans and performs autoconfiguration based on {@link EnableCassandraEventSourcing} annotation.
 */
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class CassandraEventSourcingBeansRegistrar implements ImportBeanDefinitionRegistrar {
    /**
     * Registers converters beans and performs autoconfiguration of
     * {@link DomainEventsComponent} and {@link AggregateDataComponent}
     * based on {@link EnableCassandraEventSourcing} annotation.
     */
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        Map<String, Object> attributes = importingClassMetadata.getAnnotationAttributes(EnableCassandraEventSourcing.class.getName(), false);

        if(attributes == null) {
            throw new AnnotationConfigurationException(String.format("Not able to get attributes from %s.", EnableCassandraEventSourcing.class.getName()));
        }

        String[] aggregatePackages = (String[]) attributes.get("aggregatePackages");
        List<Class<?>> aggregateClasses = AggregateTypeFilter.filterAllAggregateDataClasses(aggregatePackages);

        BeanDefinitionReaderUtils.registerWithGeneratedName(
            BeanDefinitionBuilder.genericBeanDefinition(AggregateDataComponent.class)
                .addConstructorArgValue(aggregateClasses)
                .getBeanDefinition(),
            registry
        );

        String[] eventPackages = (String[]) attributes.get("eventPackages");
        BeanDefinitionReaderUtils.registerWithGeneratedName(
            BeanDefinitionBuilder.genericBeanDefinition(DomainEventsComponent.class)
                .addConstructorArgValue(eventPackages)
                .getBeanDefinition(),
            registry
        );
    }
}
