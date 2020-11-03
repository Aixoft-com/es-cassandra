package com.aixoft.escassandra.config;

import com.aixoft.escassandra.config.EsCassandraProperties;
import com.aixoft.escassandra.config.enums.SchemaAction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.InvalidPropertiesFormatException;

@Configuration
public class TestEsCassandraConfiguration {
    @Bean
    public EsCassandraProperties esCassandraProperties() throws InvalidPropertiesFormatException {
        EsCassandraProperties properties = new EsCassandraProperties();
        properties.setKeyspace("es_test");
        properties.setSchemaAction(SchemaAction.RECREATE);

        return properties;
    }
}
