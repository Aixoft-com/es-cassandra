package com.aixoft.escassandra.config;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "escassandra")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Setter
@Getter
@Slf4j
public class EsCassandraProperties {
    String ip = "127.0.0.1";
    String port = "9042";
    String localDatacenter = "datacenter";
    String keyspace = "keyspace";
}
