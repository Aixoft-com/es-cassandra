package com.aixoft.escassandra.config;

import com.aixoft.escassandra.repository.impl.ReactiveCassandraEventDescriptorRepository;
import com.aixoft.escassandra.service.impl.ReactiveCassandraAggregateStore;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
    ReactiveCassandraEventDescriptorRepository.class,
    ReactiveCassandraAggregateStore.class})
public class EsReactiveConfiguration {
}
