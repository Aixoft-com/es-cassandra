package com.aixoft.escassandra.config;

import com.aixoft.escassandra.config.constants.RegexPattern;
import com.aixoft.escassandra.config.enums.SchemaAction;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import java.util.List;

@Validated
@ConfigurationProperties(prefix = "escassandra")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Setter
@Getter
@Slf4j
public class EsCassandraProperties {
    /**
     * List of contact points (hostname:port).
     */
    List<String> contactPoints = List.of("127.0.0.1:9042");

    /**
     * The default policy will only include nodes from this datacenter in its query plans.
     */
    @Pattern(regexp = RegexPattern.IS_ALPHANUMERIC)
    String localDatacenter = "datacenter1";

    /**
     * The name of the keyspace that the session should initially be connected to.
     */
    @Pattern(regexp = RegexPattern.IS_ALPHANUMERIC)
    String keyspace = "eskeyspace";

    /**
     * Identify schema action to be performed at startup (default is NONE).
     */
    @Min(1)
    int replicationFactor = 1;
    SchemaAction schemaAction = SchemaAction.NONE;
}

