package com.aixoft.escassandra.config;

import com.aixoft.escassandra.config.enums.SchemaAction;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.InvalidPropertiesFormatException;

@Validated
@ConfigurationProperties(prefix = "escassandra")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Setter
@Getter
@Slf4j
public class EsCassandraProperties {
    String ip = "127.0.0.1";
    String port = "9042";
    String localDatacenter = "datacenter1";
    String keyspace = "eskeyspace";
    int replicationFactor = 1;
    SchemaAction schemaAction = SchemaAction.NONE;

    public void setKeyspace(String keyspace) throws InvalidPropertiesFormatException {
        validateAlphanumericValue("keyspace", keyspace);

        this.keyspace = keyspace;
    }

    public void setLocalDatacenter(String localDatacenter) throws InvalidPropertiesFormatException {
        validateAlphanumericValue("localDatacenter", localDatacenter);

        this.localDatacenter = localDatacenter;
    }

    private void validateAlphanumericValue(String propertyName, String propertyValue) throws InvalidPropertiesFormatException {
        if(!propertyValue.matches("^\\w+$")) {
            throw new InvalidPropertiesFormatException(String.format("Property %s with value '%s' does not contain alphanumerical characters including '_'",
                propertyName,
                propertyValue));
        }
    }
}

