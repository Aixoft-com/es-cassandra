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

/**
 * Information for cassandra configuration.
 * <p>
 * Datastax Driver Configuration also can be used to customize cassandra configuration.
 *
 * @see <a href="https://docs.datastax.com/en/developer/java-driver/4.9/manual/core/configuration/">DataStax Driver Configuration</a> {@link EsCassandraProperties} properties will always take precedence over Datastax Driver Configuration.
 */
@Validated
@ConfigurationProperties(prefix = "escassandra")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Setter
@Getter
@Slf4j
public class EsCassandraProperties {
    /**
     * List of contact points (hostname:port).
     * It take precedence over property defined in driver configuration.
     */
    List<String> contactPoints = List.of("127.0.0.1:9042");

    /**
     * The default policy will only include nodes from this datacenter in its query plans.
     * It take precedence over property defined in driver configuration.
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
     * Keyspace is created with 'SimpleStrategy' and replication defined by 'replicationFactor' property.
     */
    SchemaAction schemaAction = SchemaAction.NONE;

    /**
     * Identify replica factor used for keyspace creation.
     * Relevant only if SchemaAction is different then NONE.
     */
    @Min(1)
    int replicationFactor = 1;
}

