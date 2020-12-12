package com.aixoft.escassandra.benchmark.config;

import com.aixoft.escassandra.config.EsCassandraProperties;
import com.aixoft.escassandra.config.enums.SchemaAction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BenchmarkEsCassandraConfiguration {
    @Bean
    public EsCassandraProperties esCassandraProperties() {
        EsCassandraProperties properties = new EsCassandraProperties();
        properties.setKeyspace("es_benchmark");
        properties.setSchemaAction(SchemaAction.RECREATE);

        return properties;
    }
}
