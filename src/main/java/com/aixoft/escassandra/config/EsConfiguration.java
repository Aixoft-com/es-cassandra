package com.aixoft.escassandra.config;

import com.aixoft.escassandra.repository.impl.CassandraEventDescriptorRepository;
import com.aixoft.escassandra.service.impl.CassandraAggregateStore;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
    CassandraEventDescriptorRepository.class,
    CassandraAggregateStore.class
})
public class EsConfiguration {
}
