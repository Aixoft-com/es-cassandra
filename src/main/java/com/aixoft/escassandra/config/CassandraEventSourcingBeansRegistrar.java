package com.aixoft.escassandra.config;

import com.aixoft.escassandra.aggregate.AggregateRoot;
import com.aixoft.escassandra.annotation.EnableCassandraEventSourcing;
import com.aixoft.escassandra.component.registrar.AggregateComponent;
import com.aixoft.escassandra.component.registrar.DomainEventsComponent;
import com.aixoft.escassandra.config.util.ClassTypeFilter;
import com.aixoft.escassandra.repository.converter.EventReadingConverter;
import com.aixoft.escassandra.repository.converter.EventWritingConverter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

import java.util.List;
import java.util.Map;

@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class CassandraEventSourcingBeansRegistrar implements ImportBeanDefinitionRegistrar {
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        Map<String, Object> attributes = importingClassMetadata.getAnnotationAttributes(EnableCassandraEventSourcing.class.getName(), false);

        String[] aggregatePackages = (String[]) attributes.get("aggregatePackages");
        List<Class> aggregateClasses = ClassTypeFilter.filterAllAggregateClasses(aggregatePackages, AggregateRoot.class);

        BeanDefinitionReaderUtils.registerWithGeneratedName(
            BeanDefinitionBuilder.genericBeanDefinition(AggregateComponent.class)
                .addConstructorArgValue(aggregateClasses)
                .getBeanDefinition(),
            registry
        );

        String[] eventPackages = (String[]) attributes.get("eventPackages");
        String domainEventsBeanName = BeanDefinitionReaderUtils.registerWithGeneratedName(
            BeanDefinitionBuilder.genericBeanDefinition(DomainEventsComponent.class)
                .addConstructorArgValue(eventPackages)
                .getBeanDefinition(),
            registry
        );

        registerConverters(registry, domainEventsBeanName);
    }

    private void registerConverters(BeanDefinitionRegistry registry, String domainEventsBeanName) {
        ObjectMapper objectMapper = new JsonMapper();

        BeanDefinitionReaderUtils.registerWithGeneratedName(
            BeanDefinitionBuilder.genericBeanDefinition(EventReadingConverter.class)
                .addConstructorArgReference(domainEventsBeanName)
                .addConstructorArgValue(objectMapper)
                .getBeanDefinition(),
            registry
        );

        BeanDefinitionReaderUtils.registerWithGeneratedName(
            BeanDefinitionBuilder.genericBeanDefinition(EventWritingConverter.class)
                .addConstructorArgValue(objectMapper)
                .getBeanDefinition(),
            registry
        );
    }
}
